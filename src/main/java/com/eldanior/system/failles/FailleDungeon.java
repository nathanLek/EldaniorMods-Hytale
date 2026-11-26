package com.eldanior.system.failles;

import com.eldanior.system.config.EldaniorLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plan d'un donjon de faille, genere par scripts/failles/gen_donjons.py (resources/eldanior/failles/<id>.json) : pieces vanilla
 * a coller (sans rotation), couche « sceau » (notre prefab), salles (boite, points d'apparition, monstres, mini-boss, boss) et
 * repères (arrivee, sorties, coffres, barriere). Coordonnees relatives a l'origine du donjon (FailleManager.BASE dans le monde).
 */
public final class FailleDungeon {

    /** Donjons livres avec le mod (un par rang pour l'instant : F, E et D). */
    static final String[] IDS = {"cimetiere_f", "terrier_duc_e", "necropole_d"};

    public record Piece(String prefab, int x, int y, int z, String room) {}

    public record Mob(String role, int count, int min, int max) {}

    /** Mini-boss ou boss : role, niveau (pour le rang du donjon), echelle du modele, nom affiche. */
    public record Champion(String role, int level, float scale, String name) {}

    public static final class Room {
        public String id, name;
        public boolean safe;
        public int[] min, max, center;
        public final List<int[]> spawns = new ArrayList<>();
        public final List<Mob> mobs = new ArrayList<>();
        public final List<Mob> reinforcements = new ArrayList<>();
        public int stars;
        public Champion miniboss, boss;

        boolean contains(double x, double y, double z, int margin) {
            return x >= min[0] - margin && x <= max[0] + 1 + margin && y >= min[1] - margin && y <= max[1] + 1 + margin
                    && z >= min[2] - margin && z <= max[2] + 1 + margin;
        }
    }

    public String id, name, theme, instance, overlay, markersPrefab, keyName, bossRole, bossName;
    public FailleRank rank;
    public int minutes;
    public int[] min, max, arrival, exitStart, exitEnd, boss;
    public final List<Piece> pieces = new ArrayList<>();
    /** Generateurs de monstres du plan ("x|y|z" relatif a BASE) : reglages de depart d'un modele neuf ou remis a zero. */
    public final java.util.Map<String, Generateurs.Config> generateurs = new java.util.LinkedHashMap<>();
    /** Coffres au tresor poses dans le donjon (bloc, table de butin) : cle de boss, recompenses pres du boss. */
    public record Chest(int x, int y, int z, String block, String table) {}

    /** Porte de boss du mod (Eldanior_Porte_Boss_<palier>), posee par le code (cases de remplissage comprises). */
    public record Door(String block, int x, int y, int z, int rotation) {}

    public final List<Chest> chests = new ArrayList<>();
    public Door door;
    public final List<Room> rooms = new ArrayList<>();
    public final List<Mob> overflow = new ArrayList<>();

    private static final Map<String, FailleDungeon> ALL = new LinkedHashMap<>();

    public static synchronized Map<String, FailleDungeon> all() {
        if (ALL.isEmpty()) for (String id : IDS) {
            FailleDungeon d = load(id);
            if (d != null) ALL.put(id, d);
        }
        return ALL;
    }

    public static FailleDungeon get(String id) { return all().get(id); }

    /** Donjon du rang demande, sinon celui du rang le plus proche en dessous (puis au-dessus). */
    public static FailleDungeon forRank(FailleRank rank) {
        FailleDungeon best = null;
        for (FailleDungeon d : all().values()) {
            if (d.rank == rank) return d;
            if (d.rank.ordinal() < rank.ordinal() && (best == null || d.rank.ordinal() > best.rank.ordinal())) best = d;
        }
        if (best != null) return best;
        for (FailleDungeon d : all().values()) if (best == null || d.rank.ordinal() < best.rank.ordinal()) best = d;
        return best;
    }

    private static int[] ints(JsonElement e) {
        if (e == null || e.isJsonNull()) return null;
        JsonArray a = e.getAsJsonArray();
        int[] out = new int[a.size()];
        for (int i = 0; i < out.length; i++) out[i] = a.get(i).getAsInt();
        return out;
    }

    private static void mobs(JsonElement e, List<Mob> out) {
        if (e == null || !e.isJsonArray()) return;
        for (JsonElement m : e.getAsJsonArray()) {
            JsonArray a = m.getAsJsonArray();
            out.add(new Mob(a.get(0).getAsString(), a.get(1).getAsInt(), a.size() > 2 ? a.get(2).getAsInt() : 1,
                    a.size() > 3 ? a.get(3).getAsInt() : a.size() > 2 ? a.get(2).getAsInt() : 1));
        }
    }

    private static Champion champion(JsonElement e) {
        if (e == null || !e.isJsonArray()) return null;
        JsonArray a = e.getAsJsonArray();
        return new Champion(a.get(0).getAsString(), a.get(1).getAsInt(), a.get(2).getAsFloat(), a.get(3).getAsString());
    }

    static FailleDungeon load(String id) {
        try (InputStream in = FailleDungeon.class.getResourceAsStream("/eldanior/failles/" + id + ".json")) {
            if (in == null) { EldaniorLogger.warn("[Failles] plan introuvable : " + id); return null; }
            JsonObject o = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            FailleDungeon d = new FailleDungeon();
            d.id = o.get("id").getAsString();
            d.name = o.get("name").getAsString();
            d.rank = FailleRank.of(o.get("rank").getAsString());
            d.theme = o.get("theme").getAsString();
            d.instance = o.get("instance").getAsString();
            d.minutes = o.get("minutes").getAsInt();
            d.keyName = o.get("keyName").getAsString();
            d.overlay = o.get("overlay").getAsString();
            d.markersPrefab = o.has("markersPrefab") ? o.get("markersPrefab").getAsString() : null;
            d.min = ints(o.get("min")); d.max = ints(o.get("max"));
            d.arrival = ints(o.get("arrival")); d.exitStart = ints(o.get("exitStart")); d.exitEnd = ints(o.get("exitEnd"));
            d.boss = ints(o.get("boss"));
            d.bossRole = o.has("bossRole") ? o.get("bossRole").getAsString() : null;
            d.bossName = o.has("bossName") ? o.get("bossName").getAsString() : null;
            if (o.has("generateurs"))
                for (var e : o.getAsJsonObject("generateurs").entrySet()) {
                    JsonObject g = e.getValue().getAsJsonObject();
                    d.generateurs.put(e.getKey(), new Generateurs.Config(g.get("role").getAsString(), g.get("count").getAsInt()));
                }
            if (o.has("door")) {
                JsonObject dr = o.getAsJsonObject("door");
                d.door = new Door(dr.get("block").getAsString(), dr.get("x").getAsInt(), dr.get("y").getAsInt(), dr.get("z").getAsInt(),
                        dr.get("rotation").getAsInt());
            }
            for (JsonElement p : o.getAsJsonArray("pieces")) {
                JsonObject po = p.getAsJsonObject();
                d.pieces.add(new Piece(po.get("prefab").getAsString(), po.get("x").getAsInt(), po.get("y").getAsInt(), po.get("z").getAsInt(),
                        po.get("room").getAsString()));
            }
            for (JsonElement c : o.getAsJsonArray("chests")) {
                JsonObject co = c.getAsJsonObject();
                d.chests.add(new Chest(co.get("x").getAsInt(), co.get("y").getAsInt(), co.get("z").getAsInt(), co.get("block").getAsString(),
                        co.get("table").getAsString()));
            }
            for (JsonElement r : o.getAsJsonArray("rooms")) {
                JsonObject ro = r.getAsJsonObject();
                Room room = new Room();
                room.id = ro.get("id").getAsString();
                room.name = ro.get("name").getAsString();
                room.safe = ro.has("safe") && ro.get("safe").getAsBoolean();
                room.min = ints(ro.get("min")); room.max = ints(ro.get("max")); room.center = ints(ro.get("center"));
                for (JsonElement s : ro.getAsJsonArray("spawns")) room.spawns.add(ints(s));
                mobs(ro.get("mobs"), room.mobs);
                mobs(ro.get("reinforcements"), room.reinforcements);
                room.stars = ro.has("stars") ? ro.get("stars").getAsInt() : 0;
                room.miniboss = champion(ro.get("miniboss"));
                room.boss = champion(ro.get("boss"));
                d.rooms.add(room);
            }
            for (JsonElement m : o.getAsJsonArray("overflow")) {
                JsonArray a = m.getAsJsonArray();
                d.overflow.add(new Mob(a.get(0).getAsString(), a.get(1).getAsInt(), 0, 0));
            }
            return d;
        } catch (Exception e) {
            EldaniorLogger.error("[Failles] lecture du plan " + id, e);
            return null;
        }
    }

    public Room bossRoom() {
        for (Room r : rooms) if (r.boss != null) return r;
        return rooms.get(rooms.size() - 1);
    }

    /** Nombre de monstres prevus (sans mini-boss ni boss), pour l'affichage. */
    public int mobCount() {
        int n = 0;
        for (Room r : rooms) for (Mob m : r.mobs) n += m.count();
        return n;
    }
}
