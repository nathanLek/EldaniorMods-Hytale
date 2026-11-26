package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.dojo.Mannequins;
import com.eldanior.system.util.NpcFacing;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Disciples au mannequin d'entrainement (2026-11-14, demande de l'utilisateur) : de temps en temps, un disciple au repos dont
 * le poste a un mannequin a moins de RANGE blocs s'y rend, enchaine ses kata face a lui (vrais coups au Bokken : le mannequin
 * plie et sa plaque compte les degats), puis revient a son poste. Un mannequin n'accueille qu'un disciple a la fois.
 * Appele chaque seconde par GuardManager.watchPosts ; ses kata remplacent alors ceux joues au poste (LuoNpcs.tick).
 */
final class DojoTraining {

    private DojoTraining() {}

    private static final double RANGE = 12, REACHED = 1.3;
    private static final long GIVE_UP_MS = 20_000, HIT_DELAY_MS = 350;
    private static final String[] KATA = {"Kata1", "Kata2", "Kata3", "Kata4"};
    private static final long[] KATA_MS = {1_000, 1_000, 1_100, 1_300};
    private static final Random RNG = new Random();

    private enum Mode { POST, GO, TRAIN, RETURN }

    private static final class State {
        Mode mode = Mode.POST;
        long next = System.currentTimeMillis() + 5_000 + RNG.nextInt(20_000);
        long until, since, strikeAt;
        int step;
        Mannequins.Spot spot;
        Vector3d stand;
    }

    private static final Map<String, State> STATES = new ConcurrentHashMap<>();
    /** Mannequin (cle du socle) -> poste du disciple qui s'y entraine. */
    private static final Map<String, String> TAKEN = new ConcurrentHashMap<>();

    /** Le disciple est-il pris par l'entrainement (en route, au mannequin ou de retour) ? Ses kata au poste attendent. */
    static boolean active(GuardManager.Post p) {
        State s = STATES.get(p.id);
        return s != null && s.mode != Mode.POST;
    }

    /** Thread du monde. Renvoie false quand il est a son poste sans rien a faire (le poste reprend la main). */
    static boolean tick(GuardManager.Post p, Ref<EntityStore> ref, NPCEntity npc, Vector3d at, World world, Store<EntityStore> store) {
        State s = STATES.computeIfAbsent(p.id, k -> new State());
        long now = System.currentTimeMillis();
        try {
            if (s.mode != Mode.POST && s.mode != Mode.RETURN && (!calm(ref, store) || dueling(ref))) {   // attaque, duel : on arrete
                back(p, s, npc, now);
                return false;
            }
            switch (s.mode) {
                case POST -> {
                    if (now < s.next || !calm(ref, store) || dueling(ref)) return false;
                    choose(p, s, at, world, now);
                    if (s.mode == Mode.POST) return false;
                    npc.setLeashPoint(new Vector3d(s.stand));
                }
                case GO -> {
                    npc.setLeashPoint(new Vector3d(s.stand));
                    if (now - s.since > GIVE_UP_MS || !present(s)) { back(p, s, npc, now); break; }
                    if (Math.hypot(at.x - s.stand.x, at.z - s.stand.z) > REACHED) break;
                    s.mode = Mode.TRAIN;
                    s.until = now + 20_000 + RNG.nextInt(20_000);
                    s.strikeAt = now;
                }
                case TRAIN -> {
                    npc.setLeashPoint(new Vector3d(s.stand));
                    Vector3d target = s.spot.feet().add(0, 1.2, 0);
                    NpcFacing.lookAt(ref, target, now + 2_000);
                    if (now >= s.until || !present(s)) { back(p, s, npc, now); break; }
                    if (now >= s.strikeAt) strike(p, s, ref, world, store, now);
                }
                case RETURN -> {
                    npc.setLeashPoint(p.position());
                    if (Math.hypot(at.x - p.x, at.z - p.z) < 1.5 || now - s.since > GIVE_UP_MS) {
                        s.mode = Mode.POST;
                        s.next = now + 30_000 + RNG.nextInt(60_000);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Dojo] entrainement " + p.id, e);
            release(p, s);
            s.mode = Mode.POST;
            s.next = now + 60_000;
            return false;
        }
        return true;
    }

    static void forget(String postId) {
        State s = STATES.remove(postId);
        if (s != null && s.spot != null) TAKEN.remove(s.spot.key(), postId);
    }

    private static void choose(GuardManager.Post p, State s, Vector3d at, World world, long now) {
        s.next = now + 30_000 + RNG.nextInt(30_000);
        List<Mannequins.Spot> spots = Mannequins.near(p.world, p.position(), RANGE);
        spots.removeIf(sp -> TAKEN.containsKey(sp.key()));
        if (spots.isEmpty()) return;
        Mannequins.Spot spot = spots.get(RNG.nextInt(spots.size()));
        Vector3d stand = PostMovement.standBeside(world, spot.x(), spot.y(), spot.z(), at);
        if (stand == null || TAKEN.putIfAbsent(spot.key(), p.id) != null) return;
        s.spot = spot;
        s.stand = stand;
        s.mode = Mode.GO;
        s.since = now;
        s.step = 0;
    }

    /** Un kata, puis le coup porte au mannequin quand la lame passe (HIT_DELAY_MS). */
    private static void strike(GuardManager.Post p, State s, Ref<EntityStore> ref, World world, Store<EntityStore> store, long now) {
        int k = s.step % KATA.length;
        LuoNpcs.play(p, ref, KATA[k], KATA_MS[k], store);
        s.step++;
        s.strikeAt = now + KATA_MS[k] + 150 + (s.step % 6 == 0 ? 2_500 + RNG.nextInt(2_000) : 0);   // souffle tous les 6 coups
        java.util.UUID dummy = s.spot.entity();
        float amount = damage(p, k == 3);
        EldaniorLogger.SCHEDULER.schedule(() -> world.execute(() -> {
            try {
                Store<EntityStore> st = world.getEntityStore().getStore();
                Ref<EntityStore> target = world.getEntityStore().getRefFromUUID(dummy);
                if (target == null || !target.isValid() || !ref.isValid()) return;
                Damage d = new Damage(new Damage.EntitySource(ref), DamageCause.getAssetMap().getIndex("Physical"), amount);
                DamageSystems.executeDamage(target, st, d);
            } catch (Exception e) {
                EldaniorLogger.error("[Dojo] coup au mannequin", e);
            }
        }), HIT_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /** Force du coup selon le rang martial du disciple (Apprenti ~6, Grand Maitre ~30) ; le dernier kata est appuye. */
    private static float damage(GuardManager.Post p, boolean heavy) {
        float base = 4f + MartialRanks.level(p) / 20f;
        return Math.round(base * (heavy ? 1.6f : 1f) * (0.9f + RNG.nextFloat() * 0.2f));
    }

    /** Le mannequin est-il toujours la (socle pose, entite presente) ? */
    private static boolean present(State s) {
        if (s.spot == null) return false;
        for (Mannequins.Spot sp : Mannequins.near(s.spot.key().substring(0, s.spot.key().indexOf(':')), s.spot.feet(), 0.5))
            if (sp.key().equals(s.spot.key()) && sp.entity() != null && sp.entity().equals(s.spot.entity())) return true;
        return false;
    }

    private static void back(GuardManager.Post p, State s, NPCEntity npc, long now) {
        release(p, s);
        s.mode = Mode.RETURN;
        s.since = now;
        npc.setLeashPoint(p.position());
    }

    private static void release(GuardManager.Post p, State s) {
        if (s.spot != null) TAKEN.remove(s.spot.key(), p.id);
        s.spot = null;
    }

    /** Au repos ou en train de regarder un joueur (pas en combat, pas en discussion). */
    private static boolean calm(Ref<EntityStore> ref, Store<EntityStore> store) {
        StateSupport st = StateSupport.get(ref, store);
        String state = st != null ? st.getStateName() : null;
        return state != null && (state.startsWith("Idle") || state.startsWith("Watching"));
    }

    private static boolean dueling(Ref<EntityStore> ref) {
        return com.eldanior.system.skills.runes.spells.SenseiDuel.busy(ref);
    }
}
