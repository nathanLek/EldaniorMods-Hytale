package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Balises de route des PNJ (bloc Eldanior_Route_PNJ, admins) : bloc invisible et traversable, comme le poste de PNJ ; seuls les
 * admins qui tiennent une balise (ou un poste de PNJ) en main voient les marques : colonne et anneau bleus, bouts de route en
 * orange (le PNJ y fait demi-tour), pointilles entre les balises reliees. Les PNJ de poste qui se deplacent
 * (patrouilleur, villageois, chevalier errant, chasseur de prime, criminel ; pas un aventurier recrute) suivent les balises
 * posees dans leur rayon d'action autour de leur poste (PostMovement.actionRadius) au lieu de leurs deplacements habituels,
 * des qu'il y en a au moins 2 :
 *   - une balise est reliee aux balises a moins de {@link #LINK} blocs ; la route d'un PNJ part des balises de son rayon
 *     d'action et suit toutes les balises reliees, jusqu'a {@link #MAX_REACH} blocs de son poste ;
 *   - il va de balise en balise en gardant sa direction (la plus proche devant lui, virages compris ; a un carrefour il prend
 *     parfois l'autre branche), en visant ~{@link #LOOKAHEAD} blocs plus loin pour marcher sans a-coups ; ecarte de plus de
 *     {@link #LOST} blocs (combat), il reprend a la balise la plus proche ;
 *   - au bout de la route il fait demi-tour, et apres {@link #TURN_MS} (20 min) dans un meme sens aussi ;
 *   - un villageois s'arrete parfois quelques secondes sur sa route ; bloque {@link #STUCK_MS}, il passe a la suite.
 * Registre : "monde|x|y|z" dans npc_route_blocks.properties.
 */
public final class NpcRouteBlocks {

    private NpcRouteBlocks() {}

    public static final String ITEM = "Eldanior_Route_PNJ";
    private static final String MARKER = "Eldanior_Route_Marqueur", MARKER_END = "Eldanior_Route_Bout", LINK_DOT = "Eldanior_Route_Lien";
    private static final double MARKER_RANGE = 64.0, DOT_STEP = 1.6;
    static final double LINK = 16.0;
    /** Une route part des balises du rayon d'action et suit toutes les balises reliees, jusqu'a MAX_REACH blocs du poste. */
    private static final double LOOKAHEAD = 7.0, REACHED = 2.0, MAX_DY = 24.0, MAX_REACH = 600.0, LOST = 40.0;
    private static final int MAX_NODES = 4000;
    private static final long TURN_MS = 20 * 60_000L, STUCK_MS = 5_000;
    private static final int RECENT = 24;
    private static final Random RNG = new Random();

    private static final Map<String, Boolean> blocks = new ConcurrentHashMap<>();
    /** Change a chaque balise posee ou cassee : les PNJ relisent alors les balises de leur rayon. */
    private static final AtomicInteger version = new AtomicInteger();
    private static final Map<String, Route> routes = new ConcurrentHashMap<>();
    private static Path file;

    /** Etat d'un PNJ sur sa route (thread du monde). */
    private static final class Route {
        int version = -1;
        List<Vector3d> nodes = List.of();
        Vector3d target, prev;
        final ArrayDeque<Vector3d> recent = new ArrayDeque<>();
        long dirSince, progressAt, pauseUntil;
        double best = Double.MAX_VALUE;
    }

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("npc_route_blocks.properties");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { markers(); } catch (Exception e) { EldaniorLogger.error("[NpcRoute] marques", e); }
        }, 6, 1, TimeUnit.SECONDS);
    }

    static boolean isRouteBlock(BlockType type) {
        String id = type != null ? type.getId() : null;
        return id != null && (id.equals(ITEM) || id.endsWith("*" + ITEM) || id.startsWith(ITEM + "_State"));
    }

    // ===== Suivi de route (PostMovement.tick, thread du monde) =====

    /** PNJ en combat ou occupe : l'attente ne compte pas comme un blocage. */
    static void hold(GuardManager.Post p, long now) {
        Route r = routes.get(p.id);
        if (r != null) r.progressAt = now;
    }

    /** Fait suivre la route au PNJ ; false s'il n'y a pas de route (moins de 2 balises dans son rayon d'action). */
    static boolean follow(GuardManager.Post p, NPCEntity npc, Vector3d at, long now) {
        if (blocks.isEmpty()) { routes.remove(p.id); return false; }
        Route r = routes.computeIfAbsent(p.id, k -> new Route());
        int v = version.get();
        if (r.version != v) {
            r.version = v;
            r.nodes = network(p.world, p.position(), PostMovement.actionRadius(p.type));
            r.target = null;
        }
        if (r.nodes.size() < 2) return false;
        if (r.pauseUntil > now) { npc.setLeashPoint(new Vector3d(at)); return true; }
        if (r.target == null) {                                                               // depart : balise la plus proche
            r.target = nearest(r.nodes, at);
            r.prev = null;
            r.recent.clear();
            r.dirSince = now;
            r.best = Double.MAX_VALUE;
            r.progressAt = now;
        }
        double d = Math.hypot(r.target.x - at.x, r.target.z - at.z);
        if (d > LOST) {                                                                       // ecarte (combat, chute) : reprend au plus proche
            r.target = nearest(r.nodes, at);
            r.prev = null;
            r.recent.clear();
            r.best = Double.MAX_VALUE;
            r.progressAt = now;
            d = Math.hypot(r.target.x - at.x, r.target.z - at.z);
        }
        boolean reached = d < REACHED;
        if (!reached && d < r.best - 0.5) { r.best = d; r.progressAt = now; }
        else if (!reached && now - r.progressAt > STUCK_MS) reached = true;                 // bloque : on passe a la suite
        if (now - r.dirSince > TURN_MS) turn(r, now);                                       // 20 min dans ce sens : demi-tour
        else if (reached) advance(p, r, now);
        npc.setLeashPoint(new Vector3d(r.target));
        return true;
    }

    /** Prochain point : balises enchainees devant le PNJ jusqu'a ~LOOKAHEAD blocs ; bout de la route : demi-tour. */
    private static void advance(GuardManager.Post p, Route r, long now) {
        Vector3d from = r.target, back = r.prev, last = null;
        remember(r, from);
        double run = 0;
        for (int i = 0; i < 16 && run < LOOKAHEAD; i++) {
            Vector3d next = pick(r.nodes, from, back != null ? dir(back, from) : null, r.recent);
            if (next == null) break;
            run += from.distance(next);
            remember(r, next);
            back = from;
            from = next;
            last = next;
        }
        if (last == null) { turn(r, now); return; }
        r.prev = back;
        r.target = last;
        r.best = Double.MAX_VALUE;
        r.progressAt = now;
        if (p.type.strolls() && RNG.nextInt(6) == 0) r.pauseUntil = now + 3_000 + RNG.nextInt(4_000);   // le villageois flane
    }

    /** Demi-tour : il repart vers la balise d'ou il venait (ou la plus proche autre balise au depart). */
    private static void turn(Route r, long now) {
        Vector3d cur = r.target, back = r.prev;
        if (back == null) back = pick(r.nodes, cur, null, List.of(cur));
        r.recent.clear();
        remember(r, cur);
        if (back != null) { r.prev = cur; r.target = back; }
        r.dirSince = now;
        r.best = Double.MAX_VALUE;
        r.progressAt = now;
    }

    private static void remember(Route r, Vector3d n) {
        r.recent.addFirst(n);
        while (r.recent.size() > RECENT) r.recent.removeLast();
    }

    /**
     * Balise suivante : la plus proche devant (sauf au depart), pas deja parcourue dans ce sens. A un carrefour (autre balise
     * devant, dans une direction ecartee de plus de 40 degres), le PNJ prend parfois l'autre branche.
     */
    private static Vector3d pick(List<Vector3d> nodes, Vector3d from, Vector3d heading, java.util.Collection<Vector3d> recent) {
        Vector3d best = null, branch = null;
        double bestD = Double.MAX_VALUE, branchD = Double.MAX_VALUE;
        List<Vector3d> ahead = new ArrayList<>();
        for (Vector3d n : nodes) {
            double d = from.distance(n);
            if (d < 0.5 || d > LINK || recent.contains(n)) continue;
            if (heading != null && dir(from, n).dot(heading) < -0.25) continue;
            ahead.add(n);
            if (d < bestD) { bestD = d; best = n; }
        }
        if (best == null || heading == null) return best;
        Vector3d main = dir(from, best);
        for (Vector3d n : ahead) {
            if (dir(from, n).dot(main) > 0.5) continue;                                       // meme branche
            if (n.distance(best) <= from.distance(best) + 0.5) continue;                     // suite du virage, pas un carrefour
            double d = from.distance(n);
            if (d < branchD) { branchD = d; branch = n; }
        }
        return branch != null && RNG.nextInt(3) == 0 ? branch : best;
    }

    private static Vector3d dir(Vector3d a, Vector3d b) {
        Vector3d v = new Vector3d(b.x - a.x, 0, b.z - a.z);
        return v.lengthSquared() < 1e-6 ? v : v.normalize();
    }

    private static Vector3d nearest(List<Vector3d> nodes, Vector3d at) {
        Vector3d best = nodes.get(0);
        for (Vector3d n : nodes) if (n.distanceSquared(at) < best.distanceSquared(at)) best = n;
        return best;
    }

    /**
     * Reseau de route d'un PNJ : les balises dans son rayon d'action, puis toutes celles qui s'y relient de proche en proche
     * (a moins de LINK blocs), jusqu'a MAX_REACH blocs du poste : la route peut l'emmener loin de son poste.
     */
    static List<Vector3d> network(String world, Vector3d post, double radius) {
        List<Vector3d> all = nodesAround(world, post, MAX_REACH);
        List<Vector3d> out = new ArrayList<>();
        ArrayDeque<Vector3d> todo = new ArrayDeque<>();
        java.util.Set<Vector3d> seen = new java.util.HashSet<>();
        for (Vector3d n : all) {
            if (Math.hypot(n.x - post.x, n.z - post.z) <= radius && Math.abs(n.y - post.y) <= MAX_DY && seen.add(n)) todo.add(n);
        }
        while (!todo.isEmpty() && out.size() < MAX_NODES) {
            Vector3d n = todo.poll();
            out.add(n);
            for (Vector3d m : all) if (!seen.contains(m) && n.distance(m) <= LINK) { seen.add(m); todo.add(m); }
        }
        return out;
    }

    /** Balises du monde dans le rayon (a plat) autour du centre. */
    static List<Vector3d> nodesAround(String world, Vector3d center, double radius) {
        List<Vector3d> out = new ArrayList<>();
        for (String k : blocks.keySet()) {
            if (!world.equals(NpcPostBlocks.world(k))) continue;
            Vector3d n = NpcPostBlocks.center(k);
            if (Math.hypot(n.x - center.x, n.z - center.z) <= radius && Math.abs(n.y - center.y) <= MAX_DY) out.add(n);
        }
        return out;
    }

    // ===== Registre =====

    static void register(String key) {
        blocks.put(key, Boolean.TRUE);
        version.incrementAndGet();
        save();
    }

    static void unregister(String key) {
        if (blocks.remove(key) == null) return;
        version.incrementAndGet();
        save();
    }

    /** Chaque seconde : marque des balises pour les seuls admins proches qui tiennent une balise en main. */
    private static void markers() {
        if (blocks.isEmpty()) return;
        Map<String, List<String>> byWorld = new ConcurrentHashMap<>();
        for (String k : blocks.keySet()) byWorld.computeIfAbsent(NpcPostBlocks.world(k), w -> new ArrayList<>()).add(k);
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
                List<Ref<EntityStore>> holders = new ArrayList<>();
                List<Vector3d> where = new ArrayList<>();
                for (PlayerRef pr : admins) {
                    Ref<EntityStore> r = pr.getReference();
                    Vector3d pos = com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
                    if (r == null || !r.isValid() || pos == null) continue;
                    Player player = store.getComponent(r, Player.getComponentType());
                    var hand = player != null ? player.getInventory().getActiveHotbarItem() : null;
                    if (hand != null && !hand.isEmpty() && (ITEM.equals(hand.getItemId()) || NpcPostBlocks.ITEM.equals(hand.getItemId()))) {
                        holders.add(r);                                                      // balise ou poste de PNJ en main
                        where.add(pos);
                    }
                }
                if (holders.isEmpty()) return;
                List<Vector3d> pts = new ArrayList<>();
                for (String k : keys) pts.add(NpcPostBlocks.center(k));
                for (int a = 0; a < pts.size(); a++) {
                    Vector3d at = new Vector3d(pts.get(a)).add(0, 0.05, 0);
                    List<Ref<EntityStore>> viewers = new ArrayList<>();
                    for (int i = 0; i < holders.size(); i++)
                        if (where.get(i).distanceSquared(at) <= MARKER_RANGE * MARKER_RANGE) viewers.add(holders.get(i));
                    if (viewers.isEmpty()) continue;
                    int links = 0;
                    for (int b = 0; b < pts.size(); b++) {
                        if (b == a || pts.get(a).distance(pts.get(b)) > LINK) continue;
                        links++;
                        if (b < a) continue;                                                       // chaque lien une seule fois
                        Vector3d from = pts.get(a), to = pts.get(b);
                        int n = (int) (from.distance(to) / DOT_STEP);
                        for (int i = 1; i < n; i++) {                                             // pointilles du lien
                            Vector3d d = new Vector3d(from).lerp(to, i / (double) n).add(0, 0.35, 0);
                            emit(LINK_DOT, d, viewers, store, 1.0f);
                        }
                    }
                    emit(links <= 1 ? MARKER_END : MARKER, at, viewers, store, 1.6f);            // bout de route : orange
                }
            });
        }
    }

    private static void emit(String id, Vector3d at, List<Ref<EntityStore>> viewers, Store<EntityStore> store, float scale) {
        try {
            ParticleUtil.spawnParticleEffect(id, at.x, at.y, at.z, 0f, 0f, 0f, scale, null, null, viewers, store, 1.6f);
        } catch (Exception ex) {
            EldaniorLogger.error("[NpcRoute] marque", ex);
        }
    }

    private static synchronized void save() {
        if (file == null) return;
        try {
            if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
            Properties props = new Properties();
            props.setProperty("_version", "1");
            for (String k : blocks.keySet()) props.setProperty(k, "1");
            PersistenceUtils.writeAtomicWithBackup(file, props, "Eldanior - balises de route des PNJ");
        } catch (Exception e) {
            EldaniorLogger.error("[NpcRoute] sauvegarde", e);
        }
    }

    private static void load() {
        blocks.clear();
        if (file != null && Files.exists(file)) {
            try {
                Properties props = new Properties();
                try (InputStream in = Files.newInputStream(file)) { props.load(in); }
                for (String k : props.stringPropertyNames()) if (!k.startsWith("_")) blocks.put(k, Boolean.TRUE);
            } catch (Exception e) {
                EldaniorLogger.error("[NpcRoute] chargement", e);
            }
        }
        version.incrementAndGet();
    }

    private static boolean admin(PlayerRef pr) {
        return pr != null && pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
    }

    // ===== Evenements du bloc =====

    /** Seuls les admins posent une balise ; elle est enregistree et l'admin voit combien de balises elle relie. */
    public static class Place extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        public Place() { super(PlaceBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl PlaceBlockEvent event) {
            ItemStack item = event.getItemInHand();
            if (item == null || !ITEM.equals(item.getItemId())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            PlayerRef pr = ref != null ? store.getComponent(ref, PlayerRef.getComponentType()) : null;
            World world = player != null ? player.getWorld() : null;
            if (world == null || !admin(pr)) {
                event.setCancelled(true);
                if (pr != null) pr.sendMessage(Message.raw("Seuls les admins posent des balises de route."));
                return;
            }
            Vector3i b = event.getTargetBlock();
            String key = NpcPostBlocks.key(world.getName(), b.x(), b.y(), b.z());
            register(key);
            int linked = nodesAround(world.getName(), NpcPostBlocks.center(key), LINK).size() - 1;
            pr.sendMessage(Message.raw("Balise de route posée : " + Math.max(0, linked) + " autre(s) balise(s) à moins de "
                    + (int) LINK + " blocs." + (linked <= 0 ? " Posez la suivante à " + (int) LINK + " blocs au plus." : "")));
        }

        @Override
        public Query<EntityStore> getQuery() { return Player.getComponentType(); }
    }

    /** Seuls les admins cassent une balise ; elle sort du registre. */
    public static class Break extends EntityEventSystem<EntityStore, BreakBlockEvent> {
        public Break() { super(BreakBlockEvent.class); }

        @Override
        public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> chunk, @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl CommandBuffer<EntityStore> cb, @NonNullDecl BreakBlockEvent event) {
            if (!isRouteBlock(event.getBlockType())) return;
            Player player = chunk.getComponent(index, Player.getComponentType());
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            PlayerRef pr = ref != null ? store.getComponent(ref, PlayerRef.getComponentType()) : null;
            World world = player != null ? player.getWorld() : null;
            if (world == null || !admin(pr)) { event.setCancelled(true); return; }
            Vector3i b = event.getTargetBlock();
            unregister(NpcPostBlocks.key(world.getName(), b.x(), b.y(), b.z()));
            pr.sendMessage(Message.raw("Balise de route retirée."));
        }

        @Override
        public Query<EntityStore> getQuery() { return Archetype.empty(); }
    }
}
