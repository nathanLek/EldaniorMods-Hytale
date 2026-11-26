package com.eldanior.system.dojo;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Mannequins d'entrainement du Dojo (2026-11-14, demande de l'utilisateur : « les disciples s'entrainent dessus, les joueurs
 * testent leurs attaques, les coups donnent de l'energie de signature, style un peu jap »).
 *
 * Le joueur pose un bloc (socle, Eldanior_Mannequin_Dojo) ; le mannequin lui-meme est un PNJ immobile du meme nom, debout sur le
 * socle, pour que les armes et les runes le touchent comme une vraie cible (le moteur joue son animation Hurt : il plie et
 * revient). Comme les familiers, l'entite n'est jamais sauvegardee (NonSerialized) : elle est recreee quand un joueur approche ;
 * seules les positions des socles sont gardees ici (eldanior_data/mannequins.json).
 *
 * Coups recus (MannequinDamage) : le mannequin ne meurt jamais (vie remise au plein avant chaque coup), sa plaque affiche le
 * dernier coup et le total des 10 dernieres secondes, et un coup porte par un joueur lui rend ENERGY_PER_HIT de son energie de
 * signature maximale (en plus de ce que l'arme donne d'elle-meme). Les disciples des postes proches viennent s'y exercer
 * (guards.DojoTraining).
 */
public final class Mannequins {

    private Mannequins() {}

    public static final String ID = "Eldanior_Mannequin_Dojo";
    /** Dessus du socle, en blocs (gen_mannequin.FLOOR = 6/32). */
    static final double FLOOR = 6 / 32.0;
    private static final double SHOW_RANGE = 64;
    /** Part de l'energie de signature maximale rendue par coup d'un joueur : 20 coups remplissent la jauge. */
    static final float ENERGY_PER_HIT = 0.05f;
    private static final long WINDOW_MS = 10_000, NAME_RESET_MS = 12_000;
    static final String IDLE_NAME = "Mannequin d'entraînement";

    /** Un socle pose. Champs transients : entite et coups recus, tenus sur le thread du monde. */
    static final class Dummy {
        final String world;
        final int x, y, z;
        float yaw;
        volatile UUID entity;
        final ArrayDeque<long[]> hits = new ArrayDeque<>();      // {instant, degats x100}
        long lastHit;
        float lastAmount;
        boolean named = true;                                     // plaque au repos deja posee

        Dummy(String world, int x, int y, int z, float yaw) {
            this.world = world; this.x = x; this.y = y; this.z = z; this.yaw = yaw;
        }

        String key() { return key(world, x, y, z); }

        static String key(String world, int x, int y, int z) { return world + ":" + x + ":" + y + ":" + z; }

        /** Pieds du mannequin : centre du dessus du socle. */
        Vector3d feet() { return new Vector3d(x + 0.5, y + FLOOR, z + 0.5); }
    }

    private static final Map<String, Dummy> DUMMIES = new ConcurrentHashMap<>();
    private static final Map<UUID, Dummy> BY_ENTITY = new ConcurrentHashMap<>();
    /** Vrai pendant spawnNPC : l'entite ajoutee est la notre, pas un doublon revenu avec son chunk (MannequinDamage.Entities). */
    static volatile boolean spawning;
    private static Path file;

    // ===== Cycle de vie =====

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("mannequins.json");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                tick();
            } catch (Exception e) {
                EldaniorLogger.error("[Mannequin] boucle", e);
            }
        }, 5, 1, TimeUnit.SECONDS);
        EldaniorLogger.info("[Mannequin] " + DUMMIES.size() + " mannequin(s) d'entrainement charge(s).");
    }

    static boolean isBlock(BlockType type) { return type != null && ID.equals(type.getId()); }

    /** Ce PNJ est-il un mannequin d'entrainement ? */
    public static boolean isDummy(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) return false;
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        return npc != null && ID.equals(npc.getRoleName());
    }

    static boolean isCurrent(UUID entity) { return entity != null && BY_ENTITY.containsKey(entity); }

    static Dummy of(UUID entity) { return entity != null ? BY_ENTITY.get(entity) : null; }

    // ===== Pose et casse (thread du monde) =====

    static void placed(World world, Vector3i at, Vector3d placer) {
        if (DUMMIES.containsKey(Dummy.key(world.getName(), at.x(), at.y(), at.z()))) return;
        Dummy d = new Dummy(world.getName(), at.x(), at.y(), at.z(), 0f);
        if (placer != null) d.yaw = yawToward(d.feet(), placer);                         // il fait face a celui qui le pose
        DUMMIES.put(d.key(), d);
        save();
        Store<EntityStore> store = world.getEntityStore().getStore();
        spawn(world, d, store);
    }

    static void broken(World world, Vector3i at) {
        Dummy d = DUMMIES.remove(Dummy.key(world.getName(), at.x(), at.y(), at.z()));
        if (d == null) return;
        despawn(world, d);
        save();
    }

    // ===== Boucle (chaque seconde) =====

    private static void tick() {
        Map<String, List<Dummy>> byWorld = new HashMap<>();
        for (Dummy d : DUMMIES.values()) byWorld.computeIfAbsent(d.world, w -> new ArrayList<>()).add(d);
        byWorld.forEach((name, list) -> {
            World world = Universe.get().getWorld(name);
            if (world != null) world.execute(() -> list.forEach(d -> {
                try {
                    refresh(world, d);
                } catch (Exception e) {
                    EldaniorLogger.error("[Mannequin] " + d.key(), e);
                }
            }));
        });
    }

    /** Socle disparu sans joueur (explosion...) : on oublie. Sinon : entite presente pres des joueurs, en place, plaque a jour. */
    private static void refresh(World world, Dummy d) {
        if (DUMMIES.get(d.key()) != d) return;
        BlockType type = WorldBlockUtil.getBlockType(world, d.x, d.y, d.z);
        if (type == null) return;                                                        // chunk non charge
        if (!isBlock(type)) { broken(world, new Vector3i(d.x, d.y, d.z)); return; }
        Store<EntityStore> store = world.getEntityStore().getStore();
        Ref<EntityStore> ref = refOf(world, d);
        if (ref == null) {
            if (playerNear(d.world, d.feet())) spawn(world, d, store);
            return;
        }
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (t != null && t.getPosition().distanceSquared(d.feet()) > 0.15 * 0.15)       // pousse (sort, joueur) : remis sur son socle
            store.putComponent(ref, Teleport.getComponentType(), new Teleport(d.feet(), new Rotation3f(0, d.yaw, 0)));
        long now = System.currentTimeMillis();
        if (!d.named && now - d.lastHit > NAME_RESET_MS) {
            d.named = true;
            d.hits.clear();
            store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(IDLE_NAME));
        }
    }

    private static Ref<EntityStore> refOf(World world, Dummy d) {
        UUID id = d.entity;
        if (id == null) return null;
        Ref<EntityStore> ref = world.getEntityStore().getRefFromUUID(id);
        if (ref == null || !ref.isValid()) { forget(d); return null; }                  // retire par le moteur (chunk decharge...)
        return ref;
    }

    private static void spawn(World world, Dummy d, Store<EntityStore> store) {
        if (refOf(world, d) != null) return;
        try {
            spawning = true;
            var pair = NPCPlugin.get().spawnNPC(store, ID, null, d.feet(), new Rotation3f(0, d.yaw, 0));
            Ref<EntityStore> ref = pair != null ? pair.first() : null;
            if (ref == null || !ref.isValid()) { EldaniorLogger.warn("[Mannequin] apparition ratee en " + d.key()); return; }
            store.ensureComponent(ref, store.getRegistry().getNonSerializedComponentType());
            UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
            if (u == null) { store.removeEntity(ref, RemoveReason.REMOVE); return; }
            d.entity = u.getUuid();
            BY_ENTITY.put(d.entity, d);
            d.named = true;
            store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(IDLE_NAME));
        } catch (Exception e) {
            EldaniorLogger.error("[Mannequin] apparition en " + d.key(), e);
        } finally {
            spawning = false;
        }
    }

    private static void despawn(World world, Dummy d) {
        Ref<EntityStore> ref = refOf(world, d);
        if (ref != null) world.getEntityStore().getStore().removeEntity(ref, RemoveReason.REMOVE);
        forget(d);
    }

    private static void forget(Dummy d) {
        UUID id = d.entity;
        if (id != null) BY_ENTITY.remove(id);
        d.entity = null;
    }

    // ===== Coups recus (MannequinDamage, thread du monde) =====

    /** Avant le coup : vie au plein, le coup ne peut pas le tuer. */
    static void beforeHit(Ref<EntityStore> ref, com.hypixel.hytale.server.core.modules.entity.damage.Damage damage, Store<EntityStore> store) {
        EntityStatMap stats = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        int health = DefaultEntityStatTypes.getHealth();
        EntityStatValue hp = stats != null ? stats.get(health) : null;
        if (hp == null) return;
        if (hp.get() < hp.getMax()) stats.setStatValue(health, hp.getMax());
        if (damage.getAmount() > hp.getMax() - 1f) damage.setAmount(hp.getMax() - 1f);
    }

    /** Apres le coup : compte des degats et plaque (« Coup 42 · 10 s : 315 (31,5/s) »). */
    static String recordHit(UUID entity, float amount) {
        Dummy d = of(entity);
        if (d == null) return null;
        long now = System.currentTimeMillis();
        d.hits.addLast(new long[]{now, Math.round(amount * 100)});
        while (!d.hits.isEmpty() && now - d.hits.peekFirst()[0] > WINDOW_MS) d.hits.removeFirst();
        long total = 0, first = now;
        for (long[] h : d.hits) { total += h[1]; first = Math.min(first, h[0]); }
        d.lastHit = now;
        d.lastAmount = amount;
        d.named = false;
        double sum = total / 100.0, span = Math.max(1.0, (now - first) / 1000.0);
        return "Coup " + fmt(amount) + " · 10 s : " + fmt(sum) + " (" + fmt(d.hits.size() > 1 ? sum / span : sum) + "/s)";
    }

    private static String fmt(double v) {
        return v >= 100 || v == Math.floor(v) ? String.valueOf(Math.round(v)) : String.format(Locale.FRANCE, "%.1f", v);
    }

    // ===== Pour les disciples (guards.DojoTraining) =====

    /** Mannequin pose : position du socle et entite (null si elle n'est pas apparue). */
    public record Spot(String key, int x, int y, int z, UUID entity) {
        public Vector3d feet() { return new Vector3d(x + 0.5, y + FLOOR, z + 0.5); }
    }

    /** Mannequins a moins de range blocs (horizontalement) de at, dans ce monde, dont l'entite est presente. */
    public static List<Spot> near(String world, Vector3d at, double range) {
        List<Spot> out = new ArrayList<>();
        for (Dummy d : DUMMIES.values()) {
            if (!d.world.equals(world) || d.entity == null || Math.abs(d.y - at.y) > 4) continue;
            if (Math.hypot(d.x + 0.5 - at.x, d.z + 0.5 - at.z) <= range) out.add(new Spot(d.key(), d.x, d.y, d.z, d.entity));
        }
        return out;
    }

    // ===== Utilitaires =====

    static float yawToward(Vector3d from, Vector3d to) {
        double dx = to.x - from.x, dz = to.z - from.z;
        if (Math.abs(dx) + Math.abs(dz) < 1e-3) dz = 1;
        float yaw = PhysicsMath.headingFromDirection(dx, dz);
        if (PhysicsMath.headingX(yaw) * dx + PhysicsMath.headingZ(yaw) * dz < 0) yaw += (float) Math.PI;
        return yaw;
    }

    private static boolean playerNear(String world, Vector3d at) {
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> ref = pr.getReference();
            World w = ref != null && ref.isValid() ? ref.getStore().getExternalData().getWorld() : null;
            Vector3d pos = w != null && w.getName().equals(world) ? PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid()) : null;
            if (pos != null && pos.distanceSquared(at) <= SHOW_RANGE * SHOW_RANGE) return true;
        }
        return false;
    }

    // ===== Sauvegarde =====

    private static void save() {
        if (file == null) return;
        BsonArray all = new BsonArray();
        for (Dummy d : DUMMIES.values()) {
            BsonDocument doc = new BsonDocument();
            doc.put("World", new BsonString(d.world));
            doc.put("X", new BsonInt32(d.x));
            doc.put("Y", new BsonInt32(d.y));
            doc.put("Z", new BsonInt32(d.z));
            doc.put("Yaw", new BsonDouble(d.yaw));
            all.add(doc);
        }
        BsonDocument root = new BsonDocument("Mannequins", all);
        EldaniorLogger.SCHEDULER.execute(() -> {                                        // file unique : ecritures dans l'ordre
            try {
                Files.createDirectories(file.getParent());
                BsonUtil.writeDocument(file, root).join();
            } catch (Exception e) {
                EldaniorLogger.error("[Mannequin] sauvegarde", e);
            }
        });
    }

    private static void load() {
        if (!Files.exists(file)) return;
        try {
            BsonDocument root = BsonUtil.readDocumentNow(file);
            if (root == null || !root.containsKey("Mannequins")) return;
            for (BsonValue value : root.getArray("Mannequins")) {
                BsonDocument doc = value.asDocument();
                Dummy d = new Dummy(doc.getString("World").getValue(), doc.getInt32("X").getValue(), doc.getInt32("Y").getValue(),
                        doc.getInt32("Z").getValue(), (float) doc.getDouble("Yaw").getValue());
                DUMMIES.put(d.key(), d);
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Mannequin] chargement", e);
        }
    }
}
