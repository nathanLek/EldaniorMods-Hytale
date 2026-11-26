package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Postes de PNJ poses par bloc (Eldanior_Poste_PNJ, admins) au lieu de /es garde poser : bloc invisible et traversable ;
 * le poser ou l'utiliser (F) ouvre NpcPostPage pour choisir le PNJ, ses options et son regard ; le casser retire le PNJ.
 * Registre : bloc -> poste de GuardManager (vide tant qu'aucun PNJ n'est choisi), dans npc_post_blocks.properties.
 * Marque au sol : quelques points blancs, chaque seconde, envoyes aux seuls admins proches qui tiennent le bloc en main.
 */
public final class NpcPostBlocks {

    private NpcPostBlocks() {}

    public static final String ITEM = "Eldanior_Poste_PNJ";
    private static final String MARKER = "Eldanior_Poste_Marqueur";                 // petits points blancs au sol
    private static final double MARKER_RANGE = 32.0;

    /** bloc ("monde|x|y|z") -> id du poste ("" : aucun PNJ choisi). */
    private static final Map<String, String> links = new ConcurrentHashMap<>();
    private static Path file;

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("npc_post_blocks.properties");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { markers(); } catch (Exception e) { EldaniorLogger.error("[NpcPost] marques", e); }
        }, 5, 1, TimeUnit.SECONDS);
    }

    public static String key(String world, int x, int y, int z) {
        return world + "|" + x + "|" + y + "|" + z;
    }

    static boolean isBlock(String key) { return links.containsKey(key); }

    /** Bloc pose : enregistre (sans PNJ). */
    static void register(String key) {
        links.putIfAbsent(key, "");
        save();
    }

    /** Poste du bloc, null s'il n'a pas de PNJ. */
    static GuardManager.Post postOf(String key) {
        String id = links.get(key);
        return id == null || id.isEmpty() ? null : GuardManager.post(id);
    }

    static void link(String key, String postId) {
        links.put(key, postId == null ? "" : postId);
        save();
    }

    /** Bloc casse : retire son PNJ et l'oublie. Thread du monde. */
    static void unregister(String key, World world) {
        String id = links.remove(key);
        if (id != null && !id.isEmpty()) GuardManager.removeById(id, world);
        save();
    }

    /** Centre du bloc, au sol. */
    static Vector3d center(String key) {
        String[] k = key.split("\\|");
        return new Vector3d(Integer.parseInt(k[1]) + 0.5, Integer.parseInt(k[2]), Integer.parseInt(k[3]) + 0.5);
    }

    static String world(String key) { return key.substring(0, key.indexOf('|')); }

    /**
     * Le bloc de poste est-il encore la, au centre donne (zones de monstre etoile, boss de donjon) ? null : section non chargee
     * (inconnu). Thread du monde.
     */
    public static Boolean postBlockAt(World world, Vector3d center) {
        int x = (int) Math.floor(center.x), y = (int) Math.floor(center.y), z = (int) Math.floor(center.z);
        var type = com.eldanior.system.util.WorldBlockUtil.getBlockType(world, x, y, z);
        if (type == null) return null;                                                                    // section non chargee
        return NpcPostEvents.isPostBlock(type);
    }

    /** Oublie le poste de ce centre (bloc disparu). Thread du monde. */
    public static void forget(World world, Vector3d center) {
        String key = key(world.getName(), (int) Math.floor(center.x), (int) Math.floor(center.y), (int) Math.floor(center.z));
        if (links.containsKey(key)) unregister(key, world);
    }

    /** Regard : vers le joueur qui pose, ou un point cardinal (nord = -Z). */
    static float yaw(String facing, Vector3d post, Vector3d player) {
        double dx, dz;
        switch (facing == null ? "" : facing) {
            case "nord" -> { dx = 0; dz = -1; }
            case "est" -> { dx = 1; dz = 0; }
            case "sud" -> { dx = 0; dz = 1; }
            case "ouest" -> { dx = -1; dz = 0; }
            default -> { dx = player.x - post.x; dz = player.z - post.z; }
        }
        if (Math.abs(dx) + Math.abs(dz) < 1e-3) dz = 1;
        float yaw = PhysicsMath.headingFromDirection(dx, dz);
        if (PhysicsMath.headingX(yaw) * dx + PhysicsMath.headingZ(yaw) * dz < 0) yaw += (float) Math.PI;   // convention du moteur
        return yaw;
    }

    /** Chaque seconde : points blancs sur chaque bloc, pour les seuls admins a moins de MARKER_RANGE qui tiennent le bloc en main. */
    private static void markers() {
        if (links.isEmpty()) return;
        Map<String, List<String>> byWorld = new ConcurrentHashMap<>();
        for (String k : links.keySet()) byWorld.computeIfAbsent(world(k), w -> new ArrayList<>()).add(k);
        for (Map.Entry<String, List<String>> e : byWorld.entrySet()) {
            World world = Universe.get().getWorld(e.getKey());
            if (world == null) continue;
            List<PlayerRef> admins = new ArrayList<>();
            for (PlayerRef pr : Universe.get().getPlayers()) {
                Ref<EntityStore> r = pr.getReference();
                if (r != null && r.isValid() && r.getStore().getExternalData().getWorld() == world
                        && pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) admins.add(pr);
            }
            if (admins.isEmpty()) continue;
            List<String> keys = e.getValue();
            world.execute(() -> {
                Store<EntityStore> store = world.getEntityStore().getStore();
                for (String k : keys) {
                    Vector3d at = center(k).add(0, 0.05, 0);
                    List<Ref<EntityStore>> viewers = new ArrayList<>();
                    for (PlayerRef pr : admins) {
                        Vector3d pos = com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
                        Ref<EntityStore> r = pr.getReference();
                        if (pos == null || r == null || !r.isValid() || pos.distanceSquared(at) > MARKER_RANGE * MARKER_RANGE) continue;
                        com.hypixel.hytale.server.core.entity.entities.Player player =
                                store.getComponent(r, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                        var hand = player != null ? player.getInventory().getActiveHotbarItem() : null;
                        if (hand != null && !hand.isEmpty() && ITEM.equals(hand.getItemId())) viewers.add(r);
                    }
                    if (viewers.isEmpty()) continue;
                    try {
                        ParticleUtil.spawnParticleEffect(MARKER, at.x, at.y, at.z, 0f, 0f, 0f, 1f, null, null, viewers, store, 1.6f);
                    } catch (Exception ex) {
                        EldaniorLogger.error("[NpcPost] marque", ex);
                    }
                }
            });
        }
    }

    private static synchronized void save() {
        if (file == null) return;
        try {
            if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
            Properties props = new Properties();
            props.setProperty("_version", "1");
            links.forEach(props::setProperty);
            PersistenceUtils.writeAtomicWithBackup(file, props, "Eldanior - postes de PNJ par bloc");
        } catch (Exception e) {
            EldaniorLogger.error("[NpcPost] sauvegarde", e);
        }
    }

    private static void load() {
        links.clear();
        if (file == null || !Files.exists(file)) return;
        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(file)) { props.load(in); }
            for (String k : props.stringPropertyNames()) if (!k.startsWith("_")) links.put(k, props.getProperty(k, ""));
        } catch (Exception e) {
            EldaniorLogger.error("[NpcPost] chargement", e);
        }
    }
}
