package com.eldanior.system.economy;

import com.eldanior.system.config.EldaniorLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Bareme central des prix (2026-11-15), meme calcul que scripts/economie/gen_economie.py (docs/economie.md).
 *
 * Donnees de base : eldanior/economie.json, embarque dans le jar et genere depuis scripts/economie/prix.json (objets, famille,
 * prix fixe des objets sans recette, recettes, types de ressource, prix forces, marge). Prix modifies en jeu (/es prix,
 * PriceEditorPage) : &lt;donnees du mod&gt;/economie/prix_jeu.json, appliques par-dessus ; le script les relit pour le document
 * (et --fusionner les recopie dans prix.json).
 *
 * Prix d'un objet, en cuivres : force (jeu, puis base) ; sinon recette = somme des ingredients x marge / quantite produite,
 * sous-produits deduits, arrondi a 2 chiffres significatifs ; sinon (pas de recette, recette circulaire) prix fixe (jeu, puis
 * base). Un etat « *Objet_State_X » vaut l'objet ; un type de ressource vaut le moins cher de ses objets.
 */
public final class PriceBook {

    private PriceBook() {}

    public enum Source {
        MONNAIE("monnaie"), FIXE("fixé"), FORCE("forcé"), CALCULE("calculé"), A_FIXER("à fixer"), INCONNU("?"), HORS("hors économie");

        public final String label;

        Source(String label) { this.label = label; }
    }

    /** Ingredient d'une recette : objet, ou type de ressource (type = true). */
    public record Ing(String id, boolean type, int qty) {}

    public record Entry(String id, int family, boolean eldanior, String excluded, Long basePrice, List<Ing> recipe, int outQty,
                        List<Ing> byproducts) {
        public boolean hasRecipe() { return recipe != null && !recipe.isEmpty(); }
    }

    /** Prix calcule : valeur (null : pas de prix), origine, modifie en jeu. */
    public record Price(Long value, Source source, boolean game) {}

    private static final String RESOURCE = "/eldanior/economie.json";
    private static final Map<String, Entry> ITEMS = new LinkedHashMap<>();
    private static final Map<String, List<String>> TYPES = new HashMap<>();
    private static final Map<String, Long> TYPE_PRICES = new HashMap<>(), BASE_FORCED = new HashMap<>(), COINS = new HashMap<>();
    private static final Map<String, List<String>> USED_BY = new HashMap<>();
    private static final Map<String, Long> GAME_FIXED = new TreeMap<>(), GAME_FORCED = new TreeMap<>();
    private static final Map<String, Price> MEMO = new HashMap<>();
    private static List<String> families = List.of();
    private static double marge = 1.3;
    private static Path file;
    private static boolean loaded;

    // ===== Chargement =====

    public static synchronized void init(Path dataDir) {
        file = dataDir.resolve("economie").resolve("prix_jeu.json");
        ITEMS.clear(); TYPES.clear(); TYPE_PRICES.clear(); BASE_FORCED.clear(); COINS.clear(); USED_BY.clear();
        GAME_FIXED.clear(); GAME_FORCED.clear(); MEMO.clear();
        try (InputStream in = PriceBook.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                EldaniorLogger.warn("[Economie] " + RESOURCE + " absent du jar : lancer scripts/economie/gen_economie.py");
                return;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            marge = root.get("marge").getAsDouble();
            List<String> fams = new ArrayList<>();
            for (JsonElement f : root.getAsJsonArray("familles")) fams.add(f.getAsString());
            families = Collections.unmodifiableList(fams);
            for (Map.Entry<String, JsonElement> c : root.getAsJsonObject("coins").entrySet()) COINS.put(c.getKey(), c.getValue().getAsLong());
            for (Map.Entry<String, JsonElement> t : root.getAsJsonObject("types").entrySet()) {
                List<String> members = new ArrayList<>();
                for (JsonElement m : t.getValue().getAsJsonArray()) members.add(m.getAsString());
                TYPES.put(t.getKey(), members);
            }
            for (Map.Entry<String, JsonElement> t : root.getAsJsonObject("typesPrix").entrySet()) TYPE_PRICES.put(t.getKey(), t.getValue().getAsLong());
            for (Map.Entry<String, JsonElement> t : root.getAsJsonObject("forces").entrySet()) BASE_FORCED.put(t.getKey(), t.getValue().getAsLong());
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("items").entrySet()) {
                JsonObject o = e.getValue().getAsJsonObject();
                List<Ing> recipe = ings(o.getAsJsonArray("r"));
                Entry entry = new Entry(e.getKey(), o.get("f").getAsInt(), o.has("o"), o.has("x") ? o.get("x").getAsString() : null,
                        o.has("p") ? o.get("p").getAsLong() : null, recipe, o.has("q") ? o.get("q").getAsInt() : 1, ings(o.getAsJsonArray("b")));
                ITEMS.put(e.getKey(), entry);
                if (recipe != null) for (Ing i : recipe) USED_BY.computeIfAbsent(i.type() ? "#" + i.id() : base(i.id()), k -> new ArrayList<>()).add(e.getKey());
            }
            loaded = true;
        } catch (Exception e) {
            EldaniorLogger.error("[Economie] lecture de " + RESOURCE, e);
        }
        loadGame();
        EldaniorLogger.info("[Economie] " + ITEMS.size() + " objets, " + (GAME_FIXED.size() + GAME_FORCED.size()) + " prix modifiés en jeu");
    }

    private static List<Ing> ings(JsonArray arr) {
        if (arr == null) return null;
        List<Ing> out = new ArrayList<>();
        for (JsonElement el : arr) {
            JsonArray a = el.getAsJsonArray();
            String id = a.get(0).getAsString();
            boolean type = id.startsWith("#");
            out.add(new Ing(type ? id.substring(1) : id, type, a.get(1).getAsInt()));
        }
        return out;
    }

    private static void loadGame() {
        if (file == null || !Files.exists(file)) return;
        try {
            JsonObject o = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            if (o.has("ressources")) for (Map.Entry<String, JsonElement> e : o.getAsJsonObject("ressources").entrySet()) GAME_FIXED.put(e.getKey(), e.getValue().getAsLong());
            if (o.has("forces")) for (Map.Entry<String, JsonElement> e : o.getAsJsonObject("forces").entrySet()) GAME_FORCED.put(e.getKey(), e.getValue().getAsLong());
        } catch (Exception e) {
            EldaniorLogger.error("[Economie] lecture de " + file, e);
        }
    }

    private static void saveGame() {
        if (file == null) return;
        try {
            Files.createDirectories(file.getParent());
            JsonObject o = new JsonObject();
            o.addProperty("_lisez_moi", "Prix modifiés en jeu (/es prix), en cuivres. Relus par scripts/economie/gen_economie.py "
                    + "(--fusionner : recopiés dans scripts/economie/prix.json).");
            JsonObject res = new JsonObject(), forces = new JsonObject();
            GAME_FIXED.forEach(res::addProperty);
            GAME_FORCED.forEach(forces::addProperty);
            o.add("ressources", res);
            o.add("forces", forces);
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            Files.writeString(file, gson.toJson(o), StandardCharsets.UTF_8);
        } catch (Exception e) {
            EldaniorLogger.error("[Economie] ecriture de " + file, e);
        }
    }

    // ===== Lecture =====

    public static synchronized boolean loaded() { return loaded; }

    public static synchronized List<String> families() { return families; }

    public static synchronized Entry entry(String id) { return ITEMS.get(id); }

    /** Objets d'une famille (index dans families()), dans l'ordre des identifiants. */
    public static synchronized List<String> ids(int family) {
        List<String> out = new ArrayList<>();
        for (Entry e : ITEMS.values()) if (e.family() == family) out.add(e.id());
        return out;
    }

    public static synchronized List<String> allIds() { return new ArrayList<>(ITEMS.keySet()); }

    /** Objets dont le prix a ete modifie en jeu. */
    public static synchronized List<String> gameIds() {
        List<String> out = new ArrayList<>(GAME_FIXED.keySet());
        for (String id : GAME_FORCED.keySet()) if (!out.contains(id)) out.add(id);
        Collections.sort(out);
        return out;
    }

    /** Recettes qui utilisent directement cet objet (ou un type de ressource dont il fait partie). */
    public static synchronized List<String> usedBy(String id) {
        List<String> out = new ArrayList<>(USED_BY.getOrDefault(id, List.of()));
        for (Map.Entry<String, List<String>> t : TYPES.entrySet())
            if (t.getValue().contains(id)) for (String u : USED_BY.getOrDefault("#" + t.getKey(), List.of())) if (!out.contains(u)) out.add(u);
        return out;
    }

    /** Prix d'un type de ressource et l'objet le moins cher qui le fixe. */
    public static synchronized Map.Entry<String, Long> cheapest(String type) {
        Map.Entry<String, Long> best = null;
        for (String m : TYPES.getOrDefault(type, List.of())) {
            Long p = price(m).value();
            if (p != null && (best == null || p < best.getValue())) best = Map.entry(m, p);
        }
        return best;
    }

    public static synchronized Price price(String id) {
        if (id == null) return new Price(null, Source.INCONNU, false);
        Price p = compute(id, new ArrayDeque<>(), new boolean[1]);
        return p;
    }

    /** Prix en cuivres, ou -1 sans prix. */
    public static long value(String id) {
        Long v = price(id).value();
        return v != null ? v : -1;
    }

    /** Etat d'un objet (*Deco_Tankard_State_Filled_Water) -> objet (Deco_Tankard). */
    public static String base(String id) {
        if (id.startsWith("*") && id.contains("_State_")) return id.substring(1, id.indexOf("_State_"));
        return id;
    }

    private static Price compute(String id, Deque<String> stack, boolean[] taint) {
        Price hit = MEMO.get(id);
        if (hit != null) return hit;
        boolean tainted = false;
        boolean game = GAME_FIXED.containsKey(id) || GAME_FORCED.containsKey(id);
        Price r = null;
        if (COINS.containsKey(id)) {
            r = new Price(COINS.get(id), Source.MONNAIE, false);
        } else if (GAME_FORCED.containsKey(id) || BASE_FORCED.containsKey(id)) {
            r = new Price(GAME_FORCED.containsKey(id) ? GAME_FORCED.get(id) : BASE_FORCED.get(id), Source.FORCE, game);
        } else if (id.startsWith("*") && id.contains("_State_")) {
            boolean[] t = new boolean[1];
            Price b = compute(base(id), stack, t);
            tainted = t[0];
            r = new Price(b.value(), b.source(), false);
        } else if (!ITEMS.containsKey(id)) {
            r = new Price(null, Source.INCONNU, false);
        } else {
            Entry e = ITEMS.get(id);
            if (e.excluded() != null) {
                r = new Price(null, Source.HORS, false);
            } else {
                if (e.hasRecipe() && stack.contains(id)) {
                    tainted = true;
                } else if (e.hasRecipe()) {
                    stack.push(id);
                    double total = 0;
                    boolean missing = false;
                    for (Ing in : e.recipe()) {
                        boolean[] t = new boolean[1];
                        Long p = in.type() ? typePrice(in.id(), stack, t) : compute(in.id(), stack, t).value();
                        tainted |= t[0];
                        if (p == null) missing = true;
                        else total += (double) p * in.qty();
                    }
                    if (e.byproducts() != null) for (Ing b : e.byproducts()) {
                        if (stack.contains(b.id())) continue;
                        boolean[] t = new boolean[1];
                        Long p = compute(b.id(), stack, t).value();
                        tainted |= t[0];
                        total -= (p != null ? p : 0) * (double) b.qty();
                    }
                    stack.pop();
                    if (!missing) r = new Price(nice(Math.max(total, 0) * marge / Math.max(1, e.outQty())), Source.CALCULE, false);
                }
                if (r == null) {
                    if (GAME_FIXED.containsKey(id)) r = new Price(GAME_FIXED.get(id), Source.FIXE, true);
                    else if (e.basePrice() != null) r = new Price(e.basePrice(), Source.FIXE, false);
                    else r = new Price(null, Source.A_FIXER, false);
                }
            }
        }
        if (!tainted || stack.isEmpty()) MEMO.put(id, r);
        taint[0] |= tainted;
        return r;
    }

    private static Long typePrice(String type, Deque<String> stack, boolean[] taint) {
        if (TYPE_PRICES.containsKey(type)) return TYPE_PRICES.get(type);
        Long best = null;
        for (String m : TYPES.getOrDefault(type, List.of())) {
            if (stack.contains(m)) { taint[0] = true; continue; }
            boolean[] t = new boolean[1];
            Long p = compute(m, stack, t).value();
            taint[0] |= t[0];
            if (p != null && (best == null || p < best)) best = p;
        }
        return best;
    }

    /** Arrondi lisible, comme le script : entier sous 100, puis 2 chiffres significatifs (arrondi au pair, comme Python). */
    public static long nice(double v) {
        if (v <= 0) return 0;
        if (v < 100) return Math.max(1, (long) Math.rint(v));
        long mag = (long) Math.pow(10, String.valueOf((long) v).length() - 2);
        return (long) (Math.rint(v / mag) * mag);
    }

    // ===== Modification (en jeu) =====

    /** Fixe le prix d'un objet : prix force pour un objet a recette, prix fixe sinon. Faux : monnaie ou objet inconnu. */
    public static synchronized boolean set(String id, long value) {
        Entry e = ITEMS.get(id);
        if (e == null || COINS.containsKey(id) || value < 0) return false;
        if (e.hasRecipe() || e.excluded() != null) {
            GAME_FORCED.put(id, value);
            GAME_FIXED.remove(id);
        } else {
            GAME_FIXED.put(id, value);
            GAME_FORCED.remove(id);
        }
        MEMO.clear();
        saveGame();
        return true;
    }

    /** Retire le prix modifie en jeu : l'objet reprend son prix de base (prix.json ou recette). */
    public static synchronized boolean reset(String id) {
        boolean changed = GAME_FIXED.remove(id) != null | GAME_FORCED.remove(id) != null;
        if (changed) {
            MEMO.clear();
            saveGame();
        }
        return changed;
    }

    public static synchronized boolean isGame(String id) { return GAME_FIXED.containsKey(id) || GAME_FORCED.containsKey(id); }
}
