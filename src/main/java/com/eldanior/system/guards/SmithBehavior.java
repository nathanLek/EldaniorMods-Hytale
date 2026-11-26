package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.NpcFacing;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vie des forgerons legendaires (GuardType.LEGENDAIRE) a leur poste, chaque seconde (GuardManager.watchPosts) :
 *   - de temps en temps, s'il y a un etabli, une forge, un four ou une enclume a moins de RANGE blocs du poste, il s'y rend
 *     et y travaille un moment (frappes de marteau, gestes Forger / Forger2 du jeu Luo_Gestes) ;
 *   - ou, s'il y a un tabouret ou une chaise, il va s'y asseoir (geste Assis, en boucle) : il est pose au point d'assise du
 *     bloc (base du bloc, comme le siege d'un joueur), puis se releve ;
 *   - puis il revient a son poste.
 * Pendant ce temps, on peut toujours lui parler (touche F). Le marteau en main droite : GuardManager.dress.
 */
final class SmithBehavior {

    private SmithBehavior() {}

    static final String HAMMER = "Tool_Hammer_Iron";
    private static final int RANGE = 8;
    private static final double REACHED = 1.3;
    private static final long STRIKE_MS = 1_400, GIVE_UP_MS = 25_000;
    private static final String[] WORKSHOP = {"Bench", "Forge", "Anvil", "Furnace"};
    private static final String[] SEATS = {"Stool", "Chair"};
    private static final Random RNG = new Random();

    private enum Mode { POST, GO_WORK, WORK, GO_SIT, SIT, RETURN }

    private static final class State {
        Mode mode = Mode.POST;
        long next = System.currentTimeMillis() + 8_000 + RNG.nextInt(15_000);
        long until, strike, since;
        Vector3d stand, look, seat;
    }

    private static final Map<String, State> STATES = new ConcurrentHashMap<>();

    /** Thread du monde. Renvoie false quand il est a son poste et que rien n'est en cours (le poste reprend la main : regard). */
    static boolean tick(GuardManager.Post p, Ref<EntityStore> ref, NPCEntity npc, Vector3d at, World world, Store<EntityStore> store) {
        State s = STATES.computeIfAbsent(p.id, k -> new State());
        long now = System.currentTimeMillis();
        try {
            switch (s.mode) {
                case POST -> {
                    if (now < s.next) return false;
                    choose(p, s, at, world, now);
                    if (s.mode == Mode.POST) return false;
                    npc.setLeashPoint(new Vector3d(s.stand));
                }
                case GO_WORK, GO_SIT -> {
                    npc.setLeashPoint(new Vector3d(s.stand));
                    if (now - s.since > GIVE_UP_MS) { back(p, s, npc, now); break; }
                    if (Math.hypot(at.x - s.stand.x, at.z - s.stand.z) > REACHED) break;
                    if (s.mode == Mode.GO_WORK) {
                        s.mode = Mode.WORK;
                        s.until = now + 18_000 + RNG.nextInt(17_000);
                    } else {
                        s.mode = Mode.SIT;
                        s.until = now + 25_000 + RNG.nextInt(25_000);
                        teleport(ref, s.seat, yawToward(s.seat, s.look), store);
                        play(ref, "Assis", store);
                    }
                }
                case WORK -> {
                    npc.setLeashPoint(new Vector3d(s.stand));
                    NpcFacing.lookAt(ref, s.look, now + 2_000);
                    if (now >= s.until) { back(p, s, npc, now); break; }
                    if (now >= s.strike) {
                        play(ref, RNG.nextInt(5) == 0 ? "Forger2" : "Forger", store);
                        s.strike = now + STRIKE_MS + RNG.nextInt(500);
                    }
                }
                case SIT -> {
                    npc.setLeashPoint(new Vector3d(s.seat));
                    if (now >= s.until) {
                        stop(ref, store);
                        teleport(ref, s.stand, yawToward(s.stand, s.look), store);
                        back(p, s, npc, now);
                        break;
                    }
                    if (Math.hypot(at.x - s.seat.x, at.z - s.seat.z) > 0.3 || Math.abs(at.y - s.seat.y) > 0.3)   // bouscule : on le rassoit
                        teleport(ref, s.seat, yawToward(s.seat, s.look), store);
                }
                case RETURN -> {
                    npc.setLeashPoint(p.position());
                    if (Math.hypot(at.x - p.x, at.z - p.z) < 1.5 || now - s.since > GIVE_UP_MS) {
                        s.mode = Mode.POST;
                        s.next = now + 20_000 + RNG.nextInt(40_000);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Forge] comportement " + p.id, e);
            s.mode = Mode.POST;
            s.next = now + 60_000;
            return false;
        }
        return true;
    }

    /** Assis : on le releve avant de le faire parler ou de le deplacer (fin de session, poste retire). */
    static void forget(String postId) {
        STATES.remove(postId);
    }

    private static void choose(GuardManager.Post p, State s, Vector3d at, World world, long now) {
        List<int[]> benches = new ArrayList<>(), seats = new ArrayList<>();
        int px = (int) Math.floor(p.x), py = (int) Math.floor(p.y), pz = (int) Math.floor(p.z);
        for (int x = px - RANGE; x <= px + RANGE; x++)
            for (int z = pz - RANGE; z <= pz + RANGE; z++)
                for (int y = py - 2; y <= py + 2; y++) {
                    BlockType t = com.eldanior.system.util.WorldBlockUtil.getBlockType(world, x, y, z);
                    String id = t != null ? t.getId() : null;
                    if (id == null) continue;
                    if (has(id, SEATS)) seats.add(new int[]{x, y, z});
                    else if (has(id, WORKSHOP)) benches.add(new int[]{x, y, z});
                }
        boolean work = !benches.isEmpty() && (seats.isEmpty() || RNG.nextInt(10) < 7);
        List<int[]> pool = work ? benches : seats;
        s.next = now + 30_000;
        if (pool.isEmpty()) return;
        int[] b = pool.get(RNG.nextInt(pool.size()));
        Vector3d stand = PostMovement.standBeside(world, b[0], b[1], b[2], at);
        if (stand == null) return;
        s.stand = stand;
        s.look = new Vector3d(b[0] + 0.5, b[1] + 0.6, b[2] + 0.5);
        s.seat = new Vector3d(b[0] + 0.5, b[1], b[2] + 0.5);
        if (!work) s.look = new Vector3d(p.x, p.y + 1.6, p.z);                 // assis, il regarde vers son poste
        s.mode = work ? Mode.GO_WORK : Mode.GO_SIT;
        s.since = now;
    }

    private static void back(GuardManager.Post p, State s, NPCEntity npc, long now) {
        s.mode = Mode.RETURN;
        s.since = now;
        npc.setLeashPoint(p.position());
    }

    private static boolean has(String id, String[] keys) {
        for (String k : keys) if (id.contains(k)) return true;
        return false;
    }

    private static float yawToward(Vector3d from, Vector3d to) {
        double dx = to.x - from.x, dz = to.z - from.z;
        if (Math.abs(dx) + Math.abs(dz) < 1e-3) dz = 1;
        float yaw = com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath.headingFromDirection(dx, dz);
        if (com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath.headingX(yaw) * dx + com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath.headingZ(yaw) * dz < 0)
            yaw += (float) Math.PI;
        return yaw;
    }

    private static void teleport(Ref<EntityStore> ref, Vector3d pos, float yaw, Store<EntityStore> store) {
        store.putComponent(ref, com.hypixel.hytale.server.core.modules.entity.teleport.Teleport.getComponentType(),
                new com.hypixel.hytale.server.core.modules.entity.teleport.Teleport(new Vector3d(pos), new Rotation3f(0, yaw, 0)));
    }

    private static void play(Ref<EntityStore> ref, String gesture, Store<EntityStore> store) {
        AnimationUtils.playAnimation(ref, AnimationSlot.Action, LuoNpcs.GESTURES, gesture, true, store);
    }

    private static void stop(Ref<EntityStore> ref, Store<EntityStore> store) {
        try { AnimationUtils.stopAnimation(ref, AnimationSlot.Action, store); } catch (Exception ignored) { }
    }
}
