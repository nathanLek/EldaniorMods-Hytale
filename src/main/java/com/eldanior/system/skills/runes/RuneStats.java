package com.eldanior.system.skills.runes;

import com.eldanior.system.config.EldaniorLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Statistiques des competences actives (runes) pour le panneau admin (2026-11-25, demande de l'utilisateur).
 *
 *  - Catalogue : eldanior/runes.json (scripts/runes/gen_runes_index.py) : nom, type, rarete, classes, sort du moteur.
 *  - Lancers : compteur par sort et par joueur, incremente par RuneSpellRuntime au lancement d'un sort de joueur.
 *    Sauvegarde dans <donnees du mod>/rune_stats.json toutes les 5 minutes (si quelque chose a change) et a l'arret.
 *    Les lancers ne sont comptes qu'a partir de l'installation du compteur.
 */
public final class RuneStats {

    private RuneStats() {}

    /** Une rune du catalogue. */
    public record Info(String id, String spell, String name, String type, String rarity, String classes) {}

    private static final Map<String, Info> BY_ID = new LinkedHashMap<>();
    private static final Map<String, Info> BY_SPELL = new HashMap<>();

    /** Sort -> lancers (tous joueurs). */
    private static final Map<String, Long> CASTS = new ConcurrentHashMap<>();
    /** Joueur -> (sort -> lancers). */
    private static final Map<UUID, Map<String, Long>> PLAYER_CASTS = new ConcurrentHashMap<>();
    /** Sort -> dernier lancer (ms). */
    private static final Map<String, Long> LAST_CAST = new ConcurrentHashMap<>();
    private static volatile long since = System.currentTimeMillis();
    private static volatile boolean dirty = false;
    private static Path file;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Charge le catalogue et les compteurs, et lance la sauvegarde periodique. */
    public static synchronized void init(Path dataDir) {
        loadCatalogue();
        file = dataDir.resolve("rune_stats.json");
        try {
            if (Files.exists(file)) {
                JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                if (root.has("depuis")) since = root.get("depuis").getAsLong();
                if (root.has("lancers")) for (var e : root.getAsJsonObject("lancers").entrySet()) CASTS.put(e.getKey(), e.getValue().getAsLong());
                if (root.has("dernier")) for (var e : root.getAsJsonObject("dernier").entrySet()) LAST_CAST.put(e.getKey(), e.getValue().getAsLong());
                if (root.has("joueurs")) {
                    for (var p : root.getAsJsonObject("joueurs").entrySet()) {
                        Map<String, Long> m = new ConcurrentHashMap<>();
                        for (var e : p.getValue().getAsJsonObject().entrySet()) m.put(e.getKey(), e.getValue().getAsLong());
                        try { PLAYER_CASTS.put(UUID.fromString(p.getKey()), m); } catch (IllegalArgumentException ignored) { }
                    }
                }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[RuneStats] lecture de rune_stats.json", e);
        }
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> { if (dirty) save(); }, 5, 5, TimeUnit.MINUTES);
    }

    private static void loadCatalogue() {
        if (!BY_ID.isEmpty()) return;
        try (InputStream in = RuneStats.class.getResourceAsStream("/eldanior/runes.json")) {
            if (in == null) return;
            var root = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            for (var el : root.getAsJsonArray("runes")) {
                var o = el.getAsJsonObject();
                Info info = new Info(o.get("id").getAsString(), o.get("spell").getAsString(), o.get("nom").getAsString(),
                        o.get("type").getAsString(), o.get("rarete").getAsString(), o.get("classes").getAsString());
                BY_ID.put(info.id(), info);
                if (!info.spell().isEmpty()) BY_SPELL.put(info.spell(), info);
            }
        } catch (Exception e) {
            EldaniorLogger.error("[RuneStats] lecture de eldanior/runes.json", e);
        }
    }

    /** Sauvegarde les compteurs (sauvegarde periodique et arret du serveur). */
    public static synchronized void save() {
        if (file == null) return;
        try {
            JsonObject root = new JsonObject();
            root.addProperty("_lisez_moi", "Lancers de runes par sort et par joueur (RuneStats), depuis la date 'depuis' (ms).");
            root.addProperty("depuis", since);
            root.add("lancers", GSON.toJsonTree(new TreeMap<>(CASTS)));
            root.add("dernier", GSON.toJsonTree(new TreeMap<>(LAST_CAST)));
            JsonObject players = new JsonObject();
            for (var e : PLAYER_CASTS.entrySet()) players.add(e.getKey().toString(), GSON.toJsonTree(new TreeMap<>(e.getValue())));
            root.add("joueurs", players);
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
            dirty = false;
        } catch (Exception e) {
            EldaniorLogger.error("[RuneStats] sauvegarde", e);
        }
    }

    /** Un joueur vient de lancer un sort de rune (RuneSpellRuntime). */
    public static void onCast(UUID player, String spell) {
        if (player == null || spell == null) return;
        CASTS.merge(spell, 1L, Long::sum);
        PLAYER_CASTS.computeIfAbsent(player, k -> new ConcurrentHashMap<>()).merge(spell, 1L, Long::sum);
        LAST_CAST.put(spell, System.currentTimeMillis());
        dirty = true;
    }

    // ================================================================================================= lecture

    public static Collection<Info> catalogue() { loadCatalogue(); return Collections.unmodifiableCollection(BY_ID.values()); }

    public static Info byId(String runeId) { loadCatalogue(); return BY_ID.get(runeId); }

    /** Rune d'un sort (null : sort interne, combo d'arme...). */
    public static Info bySpell(String spell) { loadCatalogue(); return BY_SPELL.get(spell); }

    /** Lancers d'une rune (tous joueurs). */
    public static long casts(Info rune) { return rune == null || rune.spell().isEmpty() ? 0 : CASTS.getOrDefault(rune.spell(), 0L); }

    /** Lancers d'une rune par un joueur. */
    public static long casts(UUID player, Info rune) {
        Map<String, Long> m = player != null ? PLAYER_CASTS.get(player) : null;
        return m == null || rune == null ? 0 : m.getOrDefault(rune.spell(), 0L);
    }

    /** Tous les sorts lances (sorts internes compris : combos des katanas, lames d'or...). */
    public static Map<String, Long> allCasts() { return Collections.unmodifiableMap(CASTS); }

    public static Map<String, Long> playerCasts(UUID player) {
        Map<String, Long> m = player != null ? PLAYER_CASTS.get(player) : null;
        return m == null ? Map.of() : Collections.unmodifiableMap(m);
    }

    public static long lastCast(String spell) { return LAST_CAST.getOrDefault(spell, 0L); }

    /** Debut du comptage (ms). */
    public static long since() { return since; }

    /** Remet les compteurs a zero (panneau admin). */
    public static synchronized void reset() {
        CASTS.clear();
        PLAYER_CASTS.clear();
        LAST_CAST.clear();
        since = System.currentTimeMillis();
        dirty = true;
        save();
    }
}
