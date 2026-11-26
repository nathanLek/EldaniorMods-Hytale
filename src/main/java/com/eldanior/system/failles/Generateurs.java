package com.eldanior.system.failles;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import org.joml.Vector3d;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Generateur de monstres (bloc Eldanior_Generateur_Monstres, demande de l'utilisateur, 2026-11-23) : bloc invisible et
 * traversable comme le poste de PNJ ; l'admin y choisit un monstre et un nombre (GenerateurPage). Quand un joueur approche
 * (24 blocs), les monstres apparaissent ; tous abattus, ils reviennent 1 h plus tard.
 *
 * Ou vivent les reglages :
 *  - bloc pose dans le MODELE d'une faille (monde d'edition) : fichier generateurs.json du modele (coordonnees relatives a
 *    l'origine du donjon) ; chaque faille ouverte de ce donjon le reprend ;
 *  - bloc pose ailleurs (monde principal...) : eldanior_data/generateurs.json.
 * Rien n'apparait dans un monde d'edition (on y prepare le donjon).
 */
public final class Generateurs {

    private Generateurs() {}

    public static final String ITEM = "Eldanior_Generateur_Monstres";
    public static final long RESPAWN_MS = 60 * 60_000L;
    static final double RANGE = 24, MARK_RANGE = 32;
    private static final String MARKER = "Eldanior_Poste_Marqueur";

    public record Config(String role, int count) {}

    private static final class State {
        final Set<UUID> mobs = ConcurrentHashMap.newKeySet();
        volatile long nextAt;
        volatile boolean busy;
    }

    /** Generateurs actifs : "monde|x|y|z" -> reglage (monde principal, et failles ouvertes copiees de leur modele). */
    private static final Map<String, Config> ACTIVE = new ConcurrentHashMap<>();
    /** Ceux du monde principal seulement (sauvegardes). */
    private static final Map<String, Config> WORLD = new ConcurrentHashMap<>();
    private static final Map<String, State> STATE = new ConcurrentHashMap<>();
    private static final Random RNG = new Random();
    private static Path file;

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("generateurs.json");
        try {
            if (Files.exists(file)) {
                Map<String, Config> m = new Gson().fromJson(Files.readString(file, StandardCharsets.UTF_8), new TypeToken<Map<String, Config>>() {}.getType());
                if (m != null) { WORLD.putAll(m); ACTIVE.putAll(m); }
            }
        } catch (Exception e) { EldaniorLogger.error("[Generateurs] lecture", e); }
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { tick(); } catch (Exception e) { EldaniorLogger.error("[Generateurs] tick", e); }
        }, 6, 1, TimeUnit.SECONDS);
    }

    static String key(String world, int x, int y, int z) { return world + "|" + x + "|" + y + "|" + z; }

    private static synchronized void saveWorld() {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(WORLD), StandardCharsets.UTF_8);
        } catch (Exception e) { EldaniorLogger.error("[Generateurs] sauvegarde", e); }
    }

    // ---- modele d'une faille (fichier generateurs.json dans son dossier) ----

    private static Path templateFile(String instance) {
        Path dir = FaillePack.instanceDir(instance);
        return dir == null ? null : dir.resolve("generateurs.json");
    }

    static Map<String, Config> loadTemplate(String instance) {
        Path f = templateFile(instance);
        try {
            if (f != null && Files.exists(f)) {
                Map<String, Config> m = new Gson().fromJson(Files.readString(f, StandardCharsets.UTF_8), new TypeToken<Map<String, Config>>() {}.getType());
                if (m != null) return new HashMap<>(m);
            }
        } catch (Exception e) { EldaniorLogger.error("[Generateurs] modele " + instance, e); }
        return new HashMap<>();
    }

    private static synchronized void saveTemplate(String instance, Map<String, Config> m) {
        Path f = templateFile(instance);
        if (f == null) return;
        try { Files.writeString(f, new GsonBuilder().setPrettyPrinting().create().toJson(m), StandardCharsets.UTF_8); }
        catch (Exception e) { EldaniorLogger.error("[Generateurs] modele " + instance, e); }
    }

    private static String rel(int x, int y, int z) {
        return (x - FailleManager.BASE_X) + "|" + (y - FailleManager.BASE_Y) + "|" + (z - FailleManager.BASE_Z);
    }

    // ---- lecture / reglage d'un bloc ----

    public static Config get(World w, int x, int y, int z) {
        String inst = FailleManager.editInstanceOf(w.getName());
        if (inst != null) return loadTemplate(inst).get(rel(x, y, z));
        return ACTIVE.get(key(w.getName(), x, y, z));
    }

    public static void set(World w, int x, int y, int z, Config c) {
        String inst = FailleManager.editInstanceOf(w.getName());
        if (inst != null) {
            Map<String, Config> m = loadTemplate(inst);
            if (c == null) m.remove(rel(x, y, z)); else m.put(rel(x, y, z), c);
            saveTemplate(inst, m);
            return;
        }
        String k = key(w.getName(), x, y, z);
        if (c == null) { WORLD.remove(k); ACTIVE.remove(k); STATE.remove(k); }
        else { WORLD.put(k, c); ACTIVE.put(k, c); }
        saveWorld();
    }

    /** Modele neuf ou remis a zero (sans generateurs.json) : il recoit les generateurs du plan ; un modele deja regle n'est pas touche. */
    static void seedTemplate(String instance, Map<String, Config> plan) {
        Path f = templateFile(instance);
        if (f == null || plan.isEmpty() || Files.exists(f)) return;
        saveTemplate(instance, new HashMap<>(plan));
    }

    /** Faille ouverte : ses generateurs sont ceux de son modele. */
    static void registerRift(String riftWorld, String instance) {
        for (Map.Entry<String, Config> e : loadTemplate(instance).entrySet()) {
            String[] p = e.getKey().split("\\|");
            ACTIVE.put(key(riftWorld, FailleManager.BASE_X + Integer.parseInt(p[0]), FailleManager.BASE_Y + Integer.parseInt(p[1]),
                    FailleManager.BASE_Z + Integer.parseInt(p[2])), e.getValue());
        }
    }

    /** Boss de la faille (2026-11-23) : un generateur d'un seul monstre a l'emplacement du boss, sauf si le modele en a deja un
     *  pour ce role. Il apparait a l'approche d'un joueur ; FailleManager le reconnait a son role (barre, crane, victoire). */
    static void registerBoss(String riftWorld, String instance, int[] rel, String role) {
        if (rel == null || role == null) return;
        for (Config c : loadTemplate(instance).values()) if (c.role() != null && c.role().startsWith(role)) return;
        ACTIVE.put(key(riftWorld, FailleManager.BASE_X + rel[0], FailleManager.BASE_Y + rel[1], FailleManager.BASE_Z + rel[2]), new Config(role, 1));
    }

    static void unregisterWorld(String world) {
        String prefix = world + "|";
        ACTIVE.keySet().removeIf(k -> k.startsWith(prefix) && !WORLD.containsKey(k));
        STATE.keySet().removeIf(k -> k.startsWith(prefix));
    }

    /** Monstre abattu (DeathXPSystem via FailleManager.killed) : son generateur repart pour 1 h quand le groupe est fini. */
    public static void killed(UUID mob) {
        if (mob == null) return;
        for (State s : STATE.values()) {
            if (!s.mobs.remove(mob)) continue;
            if (s.mobs.isEmpty()) s.nextAt = System.currentTimeMillis() + RESPAWN_MS;
            return;
        }
    }

    // ---- deroulement ----

    private static void tick() {
        long now = System.currentTimeMillis();
        Map<String, List<String>> byWorld = new HashMap<>();
        for (String k : ACTIVE.keySet()) byWorld.computeIfAbsent(k.substring(0, k.indexOf('|')), w -> new ArrayList<>()).add(k);
        for (Map.Entry<String, List<String>> e : byWorld.entrySet()) {
            World w = Universe.get().getWorld(e.getKey());
            if (w == null || FailleManager.isEditWorld(w.getName())) continue;
            List<PlayerRef> players = playersIn(w);
            if (players.isEmpty()) continue;
            for (String k : e.getValue()) {
                Config c = ACTIVE.get(k);
                State s = STATE.computeIfAbsent(k, x -> new State());
                if (c == null || s.busy || now < s.nextAt) continue;
                Vector3d at = pos(k);
                boolean near = false;
                for (PlayerRef pr : players) {
                    Vector3d p = PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
                    if (p != null && p.distanceSquared(at) <= RANGE * RANGE) { near = true; break; }
                }
                if (!near) continue;
                s.busy = true;
                w.execute(() -> {
                    try {
                        Store<EntityStore> store = w.getEntityStore().getStore();
                        // monstres encore la ? (abattus sans tueur joueur, chute...)
                        s.mobs.removeIf(u -> { Ref<EntityStore> r = w.getEntityStore().getRefFromUUID(u);
                            return r != null && r.isValid() && store.getComponent(r, DeathComponent.getComponentType()) != null; });
                        if (!s.mobs.isEmpty()) return;
                        if (s.nextAt == 0 || now >= s.nextAt) spawn(c, at, s, store);
                    } finally { s.busy = false; }
                });
            }
        }
        markers();
    }

    private static void spawn(Config c, Vector3d at, State s, Store<EntityStore> store) {
        int n = Math.max(1, Math.min(20, c.count()));
        for (int i = 0; i < n; i++) {
            Vector3d p = i == 0 ? at : new Vector3d(at.x + RNG.nextInt(5) - 2, at.y, at.z + RNG.nextInt(5) - 2);
            try {
                var pair = NPCPlugin.get().spawnNPC(store, c.role(), null, p, new Rotation3f(0, RNG.nextFloat() * 360f, 0));
                Ref<EntityStore> ref = pair != null ? pair.first() : null;
                if (ref == null || !ref.isValid()) continue;
                UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
                if (u != null) s.mobs.add(u.getUuid());
            } catch (Exception e) { EldaniorLogger.error("[Generateurs] apparition " + c.role(), e); }
        }
        s.nextAt = s.mobs.isEmpty() ? System.currentTimeMillis() + 30_000 : Long.MAX_VALUE;   // echec : nouvel essai dans 30 s
    }

    private static Vector3d pos(String k) {
        String[] p = k.split("\\|");
        return new Vector3d(Integer.parseInt(p[1]) + 0.5, Integer.parseInt(p[2]) + 0.05, Integer.parseInt(p[3]) + 0.5);
    }

    private static List<PlayerRef> playersIn(World w) {
        List<PlayerRef> out = new ArrayList<>();
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> r = pr.getReference();
            if (r != null && r.isValid() && r.getStore().getExternalData().getWorld() == w) out.add(pr);
        }
        return out;
    }

    /** Marque au sol des generateurs (bloc invisible) pour les admins qui tiennent le bloc en main, comme le poste de PNJ. */
    private static void markers() {
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> r = pr.getReference();
            if (r == null || !r.isValid() || !pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) continue;
            World w = r.getStore().getExternalData().getWorld();
            Vector3d me = PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
            if (w == null || me == null) continue;
            List<Vector3d> spots = new ArrayList<>();
            String inst = FailleManager.editInstanceOf(w.getName());
            if (inst != null) {
                for (String k : loadTemplate(inst).keySet()) {
                    String[] p = k.split("\\|");
                    spots.add(new Vector3d(FailleManager.BASE_X + Integer.parseInt(p[0]) + 0.5, FailleManager.BASE_Y + Integer.parseInt(p[1]) + 0.05,
                            FailleManager.BASE_Z + Integer.parseInt(p[2]) + 0.5));
                }
            } else {
                String prefix = w.getName() + "|";
                for (String k : ACTIVE.keySet()) if (k.startsWith(prefix)) spots.add(pos(k));
            }
            spots.removeIf(v -> v.distanceSquared(me) > MARK_RANGE * MARK_RANGE);
            if (spots.isEmpty()) continue;
            w.execute(() -> {
                if (!r.isValid()) return;
                Store<EntityStore> store = r.getStore();
                Player player = store.getComponent(r, Player.getComponentType());
                var hand = player != null ? player.getInventory().getActiveHotbarItem() : null;
                if (hand == null || hand.isEmpty() || !ITEM.equals(hand.getItemId())) return;
                for (Vector3d v : spots) {
                    try { ParticleUtil.spawnParticleEffect(MARKER, v.x, v.y, v.z, 0f, 0f, 0f, 1f, null, null, List.of(r), store, 1.6f); }
                    catch (Exception ex) { EldaniorLogger.error("[Generateurs] marque", ex); return; }
                }
            });
        }
    }
}
