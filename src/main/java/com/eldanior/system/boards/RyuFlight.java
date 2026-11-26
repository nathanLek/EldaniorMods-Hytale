package com.eldanior.system.boards;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Chef d'orchestre du vol de Kokuō-Ryū (2026-11-22 ; v2 « ameliore les transitions, il y a pas mal de lag »).
 *
 * Constat (code du serveur, verifie) : le moteur joue la blessure (DamageSystems.HitAnimation, a CHAQUE coup) et la mort
 * (DeathSystems) dans l'emplacement Status, et l'alerte du gabarit volant aussi ; la v1 y jouait ses manoeuvres, donc chaque
 * coup les remplacait par une blessure qui repartait au debut de la vague, puis le corps retombait sur le vol du moteur a
 * une autre phase : d'ou les a-coups. Corrections :
 *  - le modele n'a plus d'animation Hurt/HurtFly : HitAnimation ne joue rien s'il n'en trouve pas (getFirstBoundAnimationId),
 *    les coups ne touchent plus Status ; le sursaut (Flinch : tete, cou, machoire) est joue ici dans Face quand les PV baissent ;
 *  - ce systeme joue EN PERMANENCE une animation de vol dans Status (Hover, Fly, FlyFast, manoeuvres), choisie d'apres la
 *    vitesse, la montee, le virage et la hauteur mesures 4 fois par seconde : le vol du moteur (Movement) n'est plus visible ;
 *  - toutes ces animations durent exactement CYCLE_MS et partent de la meme phase de vague (gen_ryu_celeste.py) : un
 *    changement est programme pile en fin de cycle, la vague continue sans saut, le fondu (BlendingDuration) ne porte que
 *    sur la forme de la manoeuvre ;
 *  - si l'alerte (rugissement) prend Status, le vol reprend des qu'elle est finie (ActiveAnimationComponent).
 * La morsure (Action) ne touche que l'avant du dragon. Clignement (Blink) dans Face toutes les 3 a 7 s.
 * Sens du virage : lacet croissant = vers +X du modele (sa gauche) ; s'il s'incline a l'envers en jeu, TURN_SIGN = -1.
 */
public final class RyuFlight {

    private RyuFlight() {}

    public static final String ROLE = "Eldanior_Ryu_Celeste";
    /** Duree commune des animations de vol (150 images a 60 i/s, gen_ryu_celeste.CYCLE). */
    static final long CYCLE_MS = 2500;
    static final double CLIMB = 2.5, DIVE = -2.5;          // blocs/s
    static final double TURN = Math.toRadians(22);          // rad/s
    static final double LAND_HEIGHT = 9;                    // blocs
    static final double FAST = 9, MOVING = 2;               // blocs/s
    static final int TURN_SIGN = 1;
    static final long ROAR_MS = 2100, FLINCH_COOLDOWN = 1200;

    private static final class State {
        double x, y, z, hp = -1;
        float yaw;
        long at, cycleStart, nextBlink, nextRoll, lastFlinch, interruptedAt;
        String current, target;
        int targetCount;
        boolean switchScheduled;
    }

    private static final Map<Ref<EntityStore>, State> STATES = new WeakHashMap<>();
    private static final Random RNG = new Random();

    /** Thread du monde, environ toutes les 250 ms. */
    static void tick(Ref<EntityStore> ref, Store<EntityStore> store, World world) {
        if (ref == null || !ref.isValid()) return;
        State s = STATES.computeIfAbsent(ref, r -> new State());
        long now = System.currentTimeMillis();
        if (store.getComponent(ref, DeathComponent.getComponentType()) != null) { s.current = null; return; }   // mort : au moteur
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (t == null) return;
        var pos = t.getPosition();
        float yaw = t.getRotation().yaw();
        if (s.at == 0) {
            s.x = pos.x; s.y = pos.y; s.z = pos.z; s.yaw = yaw; s.at = now;
            s.nextBlink = now + 2000; s.nextRoll = now + 20_000 + RNG.nextInt(20_000);
            play(ref, store, s, "Hover", now);
            return;
        }
        double dt = Math.max(0.05, (now - s.at) / 1000.0);
        double vy = (pos.y - s.y) / dt;
        double vh = Math.hypot(pos.x - s.x, pos.z - s.z) / dt;
        double dyaw = yaw - s.yaw;
        while (dyaw > Math.PI) dyaw -= 2 * Math.PI;
        while (dyaw < -Math.PI) dyaw += 2 * Math.PI;
        double yawRate = dyaw / dt * TURN_SIGN;
        s.x = pos.x; s.y = pos.y; s.z = pos.z; s.yaw = yaw; s.at = now;

        faceLayer(ref, store, s, now);

        // L'alerte du gabarit (rugissement) a pris Status : on la laisse finir, puis le vol reprend tout de suite.
        String inStatus = active(ref, store, AnimationSlot.Status);
        if (s.current != null && inStatus != null && !inStatus.equals(s.current)) {
            if (s.interruptedAt == 0) s.interruptedAt = now;
            if (now - s.interruptedAt < ROAR_MS) return;
            s.interruptedAt = 0;
            play(ref, store, s, choose(s, vy, vh, yawRate, world, pos.x, pos.y, pos.z, now), now);
            return;
        }
        s.interruptedAt = 0;

        String want = choose(s, vy, vh, yawRate, world, pos.x, pos.y, pos.z, now);
        if (Objects.equals(want, s.target)) s.targetCount++;
        else { s.target = want; s.targetCount = 1; }
        boolean rollEnds = "FlyRoll".equals(s.current);                       // le tonneau ne dure qu'un cycle : toujours enchainer
        if ((rollEnds || (!want.equals(s.current) && s.targetCount >= 2)) && !s.switchScheduled) scheduleAtCycleEnd(ref, world, s, now);
    }

    /** Animation voulue d'apres le mouvement mesure (le tonneau se decide ici, une fois de temps en temps en vol rapide). */
    private static String choose(State s, double vy, double vh, double yawRate, World world, double x, double y, double z, long now) {
        if (vy < -0.5 && heightAboveGround(world, x, y, z) < LAND_HEIGHT) return "FlyLand";
        if (Math.abs(yawRate) > TURN) return yawRate > 0 ? "FlyTurnLeft" : "FlyTurnRight";
        if (vy > CLIMB) return "FlyClimb";
        if (vy < DIVE) return "FlyDive";
        if (vh > FAST) {
            if (now >= s.nextRoll && !"FlyRoll".equals(s.current)) { s.nextRoll = now + 25_000 + RNG.nextInt(20_000); return "FlyRoll"; }
            return "FlyFast";
        }
        return vh > MOVING ? "Fly" : "Hover";
    }

    /** Programme le changement pile en fin de cycle de l'animation en cours (meme phase de vague au depart de la suivante). */
    private static void scheduleAtCycleEnd(Ref<EntityStore> ref, World world, State s, long now) {
        long elapsed = Math.max(0, now - s.cycleStart);
        long wait = CYCLE_MS - (elapsed % CYCLE_MS);
        if (wait < 40) wait += CYCLE_MS;                                     // trop tard pour ce cycle : le suivant
        s.switchScheduled = true;
        EldaniorLogger.SCHEDULER.schedule(() -> world.execute(() -> {
            s.switchScheduled = false;
            try {
                if (!ref.isValid()) return;
                Store<EntityStore> store = world.getEntityStore().getStore();
                if (store.getComponent(ref, DeathComponent.getComponentType()) != null) return;
                String next = s.target != null ? s.target : "Fly";
                if ("FlyRoll".equals(s.current) && "FlyRoll".equals(next)) next = "FlyFast";
                play(ref, store, s, next, s.cycleStart + ((System.currentTimeMillis() - s.cycleStart + CYCLE_MS / 2) / CYCLE_MS) * CYCLE_MS);
            } catch (Exception ex) { EldaniorLogger.error("[Boss Mondial] vol (changement)", ex); }
        }), wait, TimeUnit.MILLISECONDS);
    }

    private static void play(Ref<EntityStore> ref, Store<EntityStore> store, State s, String anim, long cycleStart) {
        AnimationUtils.playAnimation(ref, AnimationSlot.Status, anim, true, store);
        s.current = anim;
        s.cycleStart = cycleStart;
    }

    /** Emplacement Face : sursaut quand les PV baissent (remplace la blessure du moteur), sinon clignement. */
    private static void faceLayer(Ref<EntityStore> ref, Store<EntityStore> store, State s, long now) {
        double hp = health(ref, store);
        if (s.hp >= 0 && hp >= 0 && hp < s.hp - 0.5 && now - s.lastFlinch > FLINCH_COOLDOWN) {
            AnimationUtils.playAnimation(ref, AnimationSlot.Face, "Flinch", true, store);
            s.lastFlinch = now;
            s.nextBlink = Math.max(s.nextBlink, now + 900);
        } else if (now >= s.nextBlink) {
            AnimationUtils.playAnimation(ref, AnimationSlot.Face, "Blink", true, store);
            s.nextBlink = now + 3000 + RNG.nextInt(4000);
        }
        if (hp >= 0) s.hp = hp;
    }

    private static double health(Ref<EntityStore> ref, Store<EntityStore> store) {
        EntityStatMap stats = store.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
        EntityStatValue v = stats != null ? stats.get(DefaultEntityStatTypes.getHealth()) : null;
        return v != null ? v.get() : -1;
    }

    private static String active(Ref<EntityStore> ref, Store<EntityStore> store, AnimationSlot slot) {
        ActiveAnimationComponent a = store.getComponent(ref, ActiveAnimationComponent.getComponentType());
        if (a == null || a.getActiveAnimations() == null) return null;
        String[] arr = a.getActiveAnimations();
        int i = slot.getValue();
        return i >= 0 && i < arr.length ? arr[i] : null;
    }

    /** Hauteur au-dessus du premier bloc plein (au plus 40 blocs sondes ; 99 si rien ou chunk absent). */
    private static double heightAboveGround(World world, double x, double y, double z) {
        int bx = (int) Math.floor(x), bz = (int) Math.floor(z), by = (int) Math.floor(y);
        for (int d = 0; d <= 40 && by - d > 0; d++) {
            BlockType b = WorldBlockUtil.getBlockType(world, bx, by - d, bz);
            if (b == null) return 99;
            if (b != BlockType.EMPTY && !"Empty".equals(b.getId())) return y - (by - d + 1);
        }
        return 99;
    }
}
