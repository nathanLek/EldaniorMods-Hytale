package com.eldanior.system.guards;

import com.eldanior.system.util.NpcFacing;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Deplacements des PNJ de poste, pilotes par leur point de laisse : le code choisit le prochain point, le role du PNJ s'y
 * rend lui-meme (Seek + recherche de chemin : vraies animations, contourne les obstacles). Seulement au repos (pas en
 * combat ni en retour au poste). Sans progres pendant {@link #STUCK_MS}, le point est abandonne.
 *   - Patrouilleur : ronde en boucle sur un cercle de {@link #PATROL_RADIUS} blocs autour du poste.
 *   - Chevalier errant et chasseur de prime : errent de point en point ({@link #ROAM_STEP}) dans {@link #ROAM_RADIUS} blocs
 *     autour du poste ; criminel : de meme, dans Criminals.ROAM_RADIUS blocs autour de son repaire.
 *   - Villageois : se promene dans {@link #VILLAGE_RADIUS} blocs ; va voir un autre PNJ (ils se regardent, comme s'ils
 *     discutaient) ou un bloc utile (etabli, coffre, culture...) qu'il regarde un moment, ou fait quelques pas.
 * Points praticables : sol plein, deux blocs d'air, ciel libre (pas dans ni sur les batiments).
 */
final class PostMovement {

    private PostMovement() {}

    static final double PATROL_RADIUS = 25.0, ROAM_RADIUS = 200.0, VILLAGE_RADIUS = 30.0;
    private static final int PATROL_POINTS = 32;
    private static final double REACHED = 2.5, ROAM_STEP = 30.0, NPC_VISIT_RANGE = 16.0, BLOCK_VISIT_RANGE = 12.0;
    private static final long STUCK_MS = 4_000;
    private static final String[] USEFUL_BLOCKS = {"Bench", "Chest", "Furnace", "Anvil", "Barrel", "Crate", "Crop", "Farmland",
            "Well", "Table", "Stall", "Cauldron", "Loom", "Workbench", "Lantern", "Sign"};
    private static final Random RNG = new Random();

    static boolean moves(GuardType t) {
        return t == GuardType.PATROUILLEUR || roams(t) || t == GuardType.VILLAGEOIS;
    }

    /** Errance autour du poste : chevalier errant, chasseur de prime, criminel. */
    static boolean roams(GuardType t) {
        return t == GuardType.CHEVALIER_ERRANT || t == GuardType.CHASSEUR_PRIME || t == GuardType.CRIMINEL;
    }

    private static double roamRadius(GuardType t) {
        return t == GuardType.CRIMINEL ? Criminals.ROAM_RADIUS : ROAM_RADIUS;
    }

    /** A l'apparition (ou au rechargement) : premier point. */
    static void start(GuardManager.Post p, NPCEntity npc, World world) {
        p.round = null;
        p.target = null;
        p.pauseUntil = 0;
        if (p.type == GuardType.PATROUILLEUR) startRound(p, npc, world);
    }

    /** Chaque seconde (thread du monde). */
    static void tick(GuardManager.Post p, Ref<EntityStore> ref, NPCEntity npc, Vector3d at, World world, Store<EntityStore> store) {
        StateSupport st = StateSupport.get(ref, store);
        String state = st != null ? st.getStateName() : null;
        long now = System.currentTimeMillis();
        if (state == null || !state.startsWith("Idle")) { p.progressAt = now; return; }        // combat, retour au poste...
        if (p.type == GuardType.PATROUILLEUR) { tickRound(p, npc, at, world, now); return; }
        if (p.pauseUntil > now) { npc.setLeashPoint(new Vector3d(at)); return; }              // il regarde / discute
        if (p.target == null) { pickNext(p, ref, at, world, store, now); if (p.target != null) npc.setLeashPoint(new Vector3d(p.target)); return; }
        double d = Math.hypot(p.target.x - at.x, p.target.z - at.z);
        boolean done = d < REACHED, stuck = false;
        if (!done && d < p.bestDistance - 0.5) { p.bestDistance = d; p.progressAt = now; }
        else if (!done && now - p.progressAt > STUCK_MS) stuck = true;
        if (done && p.lookAt != null) {                                                       // arrive : il regarde ce qu'il est venu voir
            long pause = 4_000 + RNG.nextInt(4_000);
            p.pauseUntil = now + pause;
            NpcFacing.lookAt(ref, p.lookAt, now + pause);
            if (p.visited != null) faceBack(p.visited, at, now + pause, store);
        } else if (done && roams(p.type) && RNG.nextInt(3) == 0) {
            p.pauseUntil = now + 1_500 + RNG.nextInt(2_000);                                  // il marque une pause
        }
        if (done || stuck) { p.target = null; p.lookAt = null; p.visited = null; }
        npc.setLeashPoint(new Vector3d(p.target != null ? p.target : at));
    }

    /** L'autre PNJ visite regarde aussi le villageois pendant la discussion. */
    private static void faceBack(Ref<EntityStore> other, Vector3d at, long until, Store<EntityStore> store) {
        if (other == null || !other.isValid()) return;
        NpcFacing.lookAt(other, new Vector3d(at).add(0, 1.6, 0), until);
        GuardManager.Post op = GuardManager.postOf(other, store);
        if (op != null && op.type == GuardType.VILLAGEOIS) op.pauseUntil = Math.max(op.pauseUntil, until);
    }

    private static void pickNext(GuardManager.Post p, Ref<EntityStore> ref, Vector3d at, World world, Store<EntityStore> store, long now) {
        p.bestDistance = Double.MAX_VALUE;
        p.progressAt = now;
        if (roams(p.type)) { p.target = roamPoint(p, at, world); return; }
        int roll = RNG.nextInt(10);
        if (roll < 4 && visitNpc(p, ref, at, world, store)) return;
        if (roll < 8 && visitBlock(p, at, world)) return;
        p.target = randomPoint(world, p.position(), at, VILLAGE_RADIUS, 4, 10);
    }

    // ===== Patrouilleur : ronde en cercle =====

    static void startRound(GuardManager.Post p, NPCEntity npc, World world) {
        List<Vector3d> points = new ArrayList<>();
        int py = (int) Math.floor(p.y);
        for (int i = 0; i < PATROL_POINTS; i++) {
            Vector3d w = walkableNear(world, 2 * Math.PI * i / PATROL_POINTS, p.x, py, p.z);
            if (w != null && (points.isEmpty() || points.get(points.size() - 1).distance(w) > 1.5)) points.add(w);
        }
        if (points.isEmpty()) points.add(p.position());                                     // tout est bloque : il garde son poste
        p.round = points;
        p.waypoint = nearestIndex(points, npc != null && npc.getLeashPoint() != null ? npc.getLeashPoint() : p.position());
        p.bestDistance = Double.MAX_VALUE;
        p.progressAt = System.currentTimeMillis();
        if (npc != null) npc.setLeashPoint(new Vector3d(points.get(p.waypoint)));
    }

    private static void tickRound(GuardManager.Post p, NPCEntity npc, Vector3d at, World world, long now) {
        if (p.round == null || (p.round.size() <= 1 && (now / 1000) % 10 == 0)) startRound(p, npc, world);   // terrain pas encore charge
        Vector3d target = p.round.get(p.waypoint);
        double d = Math.hypot(target.x - at.x, target.z - at.z);
        boolean next = d < REACHED;
        if (!next && d < p.bestDistance - 0.5) { p.bestDistance = d; p.progressAt = now; }
        else if (!next && now - p.progressAt > STUCK_MS) next = true;                          // bloque (angle, porte...)
        if (next) {
            p.waypoint = (p.waypoint + 1) % p.round.size();
            p.bestDistance = Double.MAX_VALUE;
            p.progressAt = now;
        }
        npc.setLeashPoint(new Vector3d(p.round.get(p.waypoint)));
    }

    /** Point praticable pour l'angle donne, au rayon de ronde si possible, sinon un peu plus pres / loin ou decale. */
    private static Vector3d walkableNear(World world, double angle, double cx, int cy, double cz) {
        double[] radii = {0, -2, 2, -4, 4, -6, -8, -10};
        double[] shifts = {0, 0.08, -0.08, 0.16, -0.16};
        for (double dr : radii)
            for (double da : shifts) {
                double r = PATROL_RADIUS + dr, a = angle + da;
                int x = (int) Math.floor(cx + Math.cos(a) * r), z = (int) Math.floor(cz + Math.sin(a) * r);
                Integer y = standY(world, x, cy, z, 4);
                if (y != null) return new Vector3d(x + 0.5, y, z + 0.5);
            }
        return null;
    }

    private static int nearestIndex(List<Vector3d> points, Vector3d at) {
        int best = 0;
        double bestD = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            double d = points.get(i).distanceSquared(at);
            if (d < bestD) { bestD = d; best = i; }
        }
        return best;
    }

    // ===== Chevalier errant, chasseur de prime =====

    /** Prochain point a ~ROAM_STEP blocs (moins pour un petit rayon) ; trop loin du poste, il y repart. */
    private static Vector3d roamPoint(GuardManager.Post p, Vector3d at, World world) {
        Vector3d home = p.position();
        double radius = roamRadius(p.type), step = Math.min(ROAM_STEP, radius / 2);
        boolean far = Math.hypot(at.x - home.x, at.z - home.z) > radius - step;
        for (int tries = 0; tries < 10; tries++) {
            double a = far ? Math.atan2(home.z - at.z, home.x - at.x) + (RNG.nextDouble() - 0.5) : RNG.nextDouble() * Math.PI * 2;
            double r = step * (0.5 + RNG.nextDouble() * 0.5);
            int x = (int) Math.floor(at.x + Math.cos(a) * r), z = (int) Math.floor(at.z + Math.sin(a) * r);
            if (Math.hypot(x - home.x, z - home.z) > radius) continue;
            Integer y = standY(world, x, (int) Math.floor(at.y), z, 8);
            if (y != null) return new Vector3d(x + 0.5, y, z + 0.5);
        }
        return null;                                                                          // reessaie a la seconde suivante
    }

    // ===== Villageois =====

    /** Va voir un autre PNJ proche (dans la ville) : il s'arrete a cote de lui et ils se regardent. */
    private static boolean visitNpc(GuardManager.Post p, Ref<EntityStore> self, Vector3d at, World world, Store<EntityStore> store) {
        List<Ref<EntityStore>> others = new ArrayList<>();
        for (Ref<EntityStore> e : new ArrayList<>(TargetUtil.getAllEntitiesInSphere(at, NPC_VISIT_RANGE, store))) {
            if (e == null || e.equals(self) || store.getComponent(e, NPCEntity.getComponentType()) == null) continue;
            TransformComponent t = store.getComponent(e, TransformComponent.getComponentType());
            if (t != null && Math.hypot(t.getPosition().x - p.x, t.getPosition().z - p.z) <= VILLAGE_RADIUS + 5) others.add(e);
        }
        if (others.isEmpty()) return false;
        Ref<EntityStore> o = others.get(RNG.nextInt(others.size()));
        Vector3d op = new Vector3d(store.getComponent(o, TransformComponent.getComponentType()).getPosition());
        Vector3d side = new Vector3d(at).sub(op);
        side.y = 0;
        if (side.lengthSquared() < 1e-4) side.set(1, 0, 0);
        side.normalize().mul(1.8);
        p.target = new Vector3d(op).add(side);
        p.lookAt = new Vector3d(op).add(0, 1.6, 0);
        p.visited = o;
        return true;
    }

    /** Va voir un bloc utile proche (etabli, coffre, culture...) et le regarde un moment. */
    private static boolean visitBlock(GuardManager.Post p, Vector3d at, World world) {
        int ax = (int) Math.floor(at.x), ay = (int) Math.floor(at.y), az = (int) Math.floor(at.z);
        for (int tries = 0; tries < 60; tries++) {
            int x = ax + RNG.nextInt((int) BLOCK_VISIT_RANGE * 2 + 1) - (int) BLOCK_VISIT_RANGE;
            int z = az + RNG.nextInt((int) BLOCK_VISIT_RANGE * 2 + 1) - (int) BLOCK_VISIT_RANGE;
            if (Math.hypot(x - p.x, z - p.z) > VILLAGE_RADIUS) continue;
            for (int y = ay - 2; y <= ay + 3; y++) {
                BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                if (t == null || t.getId() == null || !useful(t.getId())) continue;
                Vector3d stand = standBeside(world, x, y, z, at);
                if (stand == null) continue;
                p.target = stand;
                p.lookAt = new Vector3d(x + 0.5, y + 0.5, z + 0.5);
                return true;
            }
        }
        return false;
    }

    private static boolean useful(String id) {
        for (String k : USEFUL_BLOCKS) if (id.contains(k)) return true;
        return false;
    }

    /** Case praticable a cote du bloc (cote du villageois en priorite ; ici le ciel peut etre couvert : etal, auvent). */
    private static Vector3d standBeside(World world, int bx, int by, int bz, Vector3d from) {
        int[][] sides = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        Vector3d best = null;
        double bestD = Double.MAX_VALUE;
        for (int[] s : sides) {
            int x = bx + s[0], z = bz + s[1];
            for (int y = by + 1; y >= by - 1; y--) {
                if (!solid(world, x, y - 1, z) || solid(world, x, y, z) || solid(world, x, y + 1, z)) continue;
                double d = Math.hypot(x + 0.5 - from.x, z + 0.5 - from.z);
                if (d < bestD) { bestD = d; best = new Vector3d(x + 0.5, y, z + 0.5); }
                break;
            }
        }
        return best;
    }

    /** Point praticable au hasard a min..max blocs du PNJ, dans radius autour du centre. */
    private static Vector3d randomPoint(World world, Vector3d center, Vector3d at, double radius, double min, double max) {
        for (int tries = 0; tries < 10; tries++) {
            double a = RNG.nextDouble() * Math.PI * 2, r = min + RNG.nextDouble() * (max - min);
            int x = (int) Math.floor(at.x + Math.cos(a) * r), z = (int) Math.floor(at.z + Math.sin(a) * r);
            if (Math.hypot(x - center.x, z - center.z) > radius) continue;
            Integer y = standY(world, x, (int) Math.floor(at.y), z, 4);
            if (y != null) return new Vector3d(x + 0.5, y, z + 0.5);
        }
        return null;
    }

    // ===== Terrain =====

    /** Hauteur des pieds si la colonne est praticable a +/- range blocs : sol plein, 2 blocs d'air, ciel libre sur 8. */
    static Integer standY(World world, int x, int baseY, int z, int range) {
        for (int y = baseY + range; y >= baseY - range; y--) {
            if (!solid(world, x, y - 1, z) || solid(world, x, y, z) || solid(world, x, y + 1, z)) continue;
            for (int k = 2; k <= 8; k++) if (solid(world, x, y + k, z)) return null;     // sous un toit : dans un batiment
            return y;
        }
        return null;
    }

    private static boolean solid(World world, int x, int y, int z) {
        BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
        return t != null && t.getMaterial() == BlockMaterial.Solid;
    }

    static java.util.UUID uuid(Ref<EntityStore> ref, com.hypixel.hytale.component.ComponentAccessor<EntityStore> store) {
        UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
        return u != null ? u.getUuid() : null;
    }
}
