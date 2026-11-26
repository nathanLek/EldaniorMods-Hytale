package com.eldanior.system.TreasureChest;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.configs.LootTableConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tirage des coffres au tresor (refonte du 2026-11-25 ; conception : scripts/coffres/conception.py → eldanior/coffres.json).
 *
 * Les objets de chaque coffre viennent du catalogue du mod (familles et rangs calcules par gen_coffres.py).
 * Nombre d'objets : 1 ; un 2e avec la chance « second » du coffre + la moitie de la Chance du joueur (stat Butin, %) ; un 3e
 * avec le quart de la Chance du joueur. Pour chaque objet : une famille (poids du coffre), un rang (poids du coffre, parmi les
 * rangs que la famille possede dans ce coffre), un objet au hasard de ce rang ; Chance / 200 : le rang monte d'un cran.
 * Coffres sans profil (or, cles de boss) : ancien tirage de LootTableConfig.
 */
public final class ChestLoot {

    private ChestLoot() {}

    public static final int MAX_ITEMS = 3;

    /** Un objet possible : famille, rang (1..7), quantites. */
    public record Candidate(String itemId, String family, int rank, int min, int max) {}

    /** Profil d'un coffre. second = chance (%) d'un deuxieme objet. */
    public record Profile(String id, String name, long cooldownSeconds, int second, Map<String, Integer> families,
                          Map<Integer, Integer> ranks, List<Candidate> items) {}

    private static Map<String, Profile> profiles;

    private static synchronized void load() {
        if (profiles != null) return;
        Map<String, Profile> p = new LinkedHashMap<>();
        try (InputStream in = ChestLoot.class.getResourceAsStream("/eldanior/coffres.json")) {
            if (in != null) {
                JsonObject root = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                for (var e : root.getAsJsonObject("coffres").entrySet()) {
                    JsonObject o = e.getValue().getAsJsonObject();
                    Map<String, Integer> fam = new LinkedHashMap<>();
                    for (var f : o.getAsJsonObject("familles").entrySet()) fam.put(f.getKey(), f.getValue().getAsInt());
                    Map<Integer, Integer> rk = new TreeMap<>();
                    for (var r : o.getAsJsonObject("rangs").entrySet()) rk.put(Integer.parseInt(r.getKey()), r.getValue().getAsInt());
                    List<Candidate> items = new ArrayList<>();
                    for (var el : o.getAsJsonArray("objets")) {
                        JsonArray a = el.getAsJsonArray();
                        items.add(new Candidate(a.get(0).getAsString(), a.get(1).getAsString(), a.get(2).getAsInt(), a.get(3).getAsInt(), a.get(4).getAsInt()));
                    }
                    p.put(e.getKey(), new Profile(e.getKey(), o.get("nom").getAsString(), o.get("recharge").getAsLong(), o.get("second").getAsInt(),
                            fam, rk, items));
                }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Coffres] lecture de eldanior/coffres.json", e);
        }
        profiles = p;
    }

    public static Profile profile(String tableId) {
        load();
        return tableId == null ? null : profiles.get(tableId.toLowerCase(Locale.ROOT));
    }

    public static Collection<Profile> profiles() { load(); return Collections.unmodifiableCollection(profiles.values()); }

    /** Recharge d'un coffre (profil, sinon sa table). */
    public static long cooldownMillis(String tableId) {
        Profile p = profile(tableId);
        return p != null ? p.cooldownSeconds() * 1000L : LootTableConfig.getById(tableId).getCooldownMillis();
    }

    // ================================================================================================= tirage

    /** Butin d'une ouverture. luck = stat Butin du joueur (en %). */
    public static List<ItemStack> generate(String tableId, float luck) {
        Profile p = profile(tableId);
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        if (p == null) return LootTableConfig.getById(tableId).generateLoot(rng.nextLong());
        if (p.items().isEmpty()) return List.of();

        float l = Math.max(0f, luck);
        int count = 1;
        if (rng.nextFloat() * 100f < p.second() + l / 2f) {
            count++;
            if (rng.nextFloat() * 100f < l / 4f) count++;                            // le 3e : seulement avec beaucoup de chance
        }
        count = Math.min(MAX_ITEMS, count);

        List<ItemStack> out = new ArrayList<>();
        Set<String> taken = new HashSet<>();
        for (int n = 0; n < count; n++) {
            Candidate c = pick(p, pickFamily(p, taken, rng), rng, taken, rng.nextFloat() * 200f < l);
            if (c == null) c = pick(p, null, rng, taken, false);
            if (c == null) break;
            taken.add(c.itemId());
            int qty = c.min() + (c.max() > c.min() ? rng.nextInt(c.max() - c.min() + 1) : 0);
            out.add(new ItemStack(c.itemId(), Math.max(1, qty)));
        }
        return out;
    }

    private static String pickFamily(Profile p, Set<String> taken, ThreadLocalRandom rng) {
        Map<String, Integer> available = new LinkedHashMap<>();
        for (var f : p.families().entrySet()) {
            for (Candidate c : p.items()) {
                if (c.family().equals(f.getKey()) && !taken.contains(c.itemId())) { available.put(f.getKey(), f.getValue()); break; }
            }
        }
        int total = 0;
        for (int w : available.values()) total += w;
        if (total <= 0) return null;
        int r = rng.nextInt(total);
        for (var f : available.entrySet()) { r -= f.getValue(); if (r < 0) return f.getKey(); }
        return null;
    }

    /** Un objet de la famille (null = toutes) : rang selon le coffre parmi ceux disponibles, puis un objet de ce rang. */
    private static Candidate pick(Profile p, String family, ThreadLocalRandom rng, Set<String> taken, boolean upgrade) {
        List<Candidate> fam = new ArrayList<>();
        for (Candidate c : p.items()) if ((family == null || c.family().equals(family)) && !taken.contains(c.itemId())) fam.add(c);
        if (fam.isEmpty()) return null;
        Map<Integer, Integer> ranks = new TreeMap<>();
        for (var r : p.ranks().entrySet()) {
            for (Candidate c : fam) if (c.rank() == r.getKey()) { ranks.put(r.getKey(), r.getValue()); break; }
        }
        if (ranks.isEmpty()) return fam.get(rng.nextInt(fam.size()));
        int total = 0;
        for (int w : ranks.values()) total += w;
        int r = rng.nextInt(total);
        int rank = ranks.keySet().iterator().next();
        for (var e : ranks.entrySet()) { r -= e.getValue(); if (r < 0) { rank = e.getKey(); break; } }
        if (upgrade) for (int k : ranks.keySet()) if (k > rank) { rank = k; break; }   // Chance : un cran plus haut
        List<Candidate> same = new ArrayList<>();
        for (Candidate c : fam) if (c.rank() == rank) same.add(c);
        return same.get(rng.nextInt(same.size()));
    }

    // ================================================================================================= statistiques (admin)

    /** Objets possibles d'un coffre par famille. */
    public static Map<String, Integer> breakdown(String tableId) {
        Profile p = profile(tableId);
        Map<String, Integer> out = new TreeMap<>();
        if (p != null) for (Candidate c : p.items()) out.merge(c.family(), 1, Integer::sum);
        return out;
    }
}
