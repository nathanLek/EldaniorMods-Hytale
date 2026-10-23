package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sablier de Chronos — sort divin du temps (Rune_SablierChronos), dieu grec du temps. Le temps est vert.
 *
 * Une horloge divine geante (cadran emeraude de 70 blocs = la zone, trois aiguilles qui tournent dessous) apparait
 * au-dessus de la zone visee ({@link #RADIUS} blocs, comme la Porte de Niflheim) et un sablier flotte au-dessus du
 * lanceur.
 *  - Arret du temps ({@link #STOP_MS}) : les ennemis de la zone (et ceux qui y entrent) sont figes dans une stase
 *    verte (glyphe d'horloge, sable du temps) ; tous les degats qu'on leur inflige sont mis en reserve (voir
 *    DivineDamageSystem).
 *  - Pendant tout le sort, le lanceur seme en se deplacant des images remanentes figees de lui-meme (skin, posture,
 *    orientation, voile vert), serrees derriere lui, comme les silhouettes du dash : les {@link #TRAIL_COUNT} plus
 *    recentes restent visibles. Chacune de ses attaques
 *    devient un dash temporel : il apparait devant l'ennemi vise et le frappe.
 *  - Chaque etape a son son : horloge qui se materialise, arret du temps, tic-tac pendant l'arret, dash, retour en
 *    arriere (remontage), cascade, et l'eclat de temps final qui libere les ennemis.
 *  - Fin, en trois temps :
 *     1. Retour en arriere : les aiguilles tournent a rebours, une trainee temporelle relie le lanceur a sa position
 *        du lancement ; il y revient et recupere la vie perdue depuis ;
 *     2. les degats en reserve s'abattent en cascade, ennemi par ennemi, x{@link #RELEASE_MULTIPLIER} ;
 *     3. le temps se brise comme du verre : les ennemis sont enfin liberes.
 *  - Allies dans la zone : acceleration (+30 % de vitesse, endurance restauree) pendant tout le sort.
 */
public final class ChronosHourglass {

    private ChronosHourglass() {}

    // ===== Reglages =====
    private static final double TARGET_RANGE = 40.0, FALLBACK_DISTANCE = 12.0;
    static final double RADIUS = SpellFx.DIVINE_ZONE_RADIUS;
    private static final long STOP_AT = 600, STOP_MS = 30_000;
    private static final long REWIND_TELEPORT_MS = 600;       // apres la fin de l'arret : aiguilles a rebours, puis teleportation
    private static final long CASCADE_DELAY_MS = 300, CASCADE_STEP_MS = 100, BREAK_DELAY_MS = 300, LINGER_MS = 900;
    private static final float RELEASE_MULTIPLIER = 1.5f;
    private static final float TIME_SHOCK_DAMAGE = 200f;      // onde temporelle a la reprise, en plus des degats accumules
    private static final float STAMINA = 0.05f;              // endurance rendue chaque seconde aux allies
    private static final double CLOCK_HEIGHT = 22.0, HOURGLASS_HEIGHT = 2.7;
    private static final double REWIND_MAX_DISTANCE = 120.0;
    // Images remanentes : en mouvement, une silhouette figee du lanceur tous les TRAIL_STEP blocs (TRAIL_COUNT a la fois)
    private static final double TRAIL_STEP = 0.75, TRAIL_TELEPORT = 4.0;  // au-dela : teleportation (dash, retour), pas de trainee
    private static final long TRAIL_MIN_MS = 70, TRAIL_GHOST_MS = 600;
    private static final int TRAIL_COUNT = 4;
    private static final long GHOST_MS = 700;                  // silhouettes laissees par le dash
    private static final int GHOST_MAX = 16;
    // Dash temporel (a chaque attaque)
    private static final double DASH_RANGE = 16.0, DASH_MIN_ALIGN = 0.55, DASH_STOP = 1.3;
    private static final long DASH_COOLDOWN_MS = 350;
    private static final float DASH_DAMAGE = 150f;

    // ===== Assets =====
    private static final String CLOCK_PROJECTILE = "Projectile_Config_Chronos_Clock";
    private static final String HANDS_PROJECTILE = "Projectile_Config_Chronos_Hands";
    private static final String REWIND_HANDS_PROJECTILE = "Projectile_Config_Chronos_Hands_Rewind";
    private static final String HOURGLASS_PROJECTILE = "Projectile_Config_Chronos_Hourglass";
    private static final String GHOST_PROJECTILE = "Projectile_Config_Chronos_Ghost";
    private static final String FX_TIME = "Chronos_Burst";
    private static final String FX_GEAR = "Chronos_Gold";
    private static final String FX_DASH = "Chronos_Dash";
    private static final String FX_AFTERIMAGE = "Chronos_Afterimage";
    private static final String FX_SHATTER = "Chronos_Shatter";
    private static final String FROZEN_EFFECT = "Chronos_Fige";
    private static final String HASTE_EFFECT = "Chronos_Hate";
    private static final String GHOST_EFFECT = "Chronos_Remanence";
    // Sons (3D = mono positionne ; 2D = stereo joue aux joueurs proches)
    private static final String SFX_CLOCK = "SFX_Arcane_Workbench_Craft", SFX_CLOCK_OPEN = "SFX_Portal_Neutral_Open";
    private static final String SFX_CLOCK_2D = "SFX_Runebinders_Lattice_Unlock_Local";
    private static final String SFX_STOP_2D = "SFX_Void_Eviction", SFX_STOP_CRACK = "SFX_Ability_Charged_Shot_Crack", SFX_STOP_GEARS = "SFX_Emit_Goblin_Arena_Mechanism";
    private static final String SFX_TICK = "SFX_Lever_Metal_On", SFX_TOCK = "SFX_Lever_Metal_Off";
    private static final String SFX_DASH = "SFX_Daggers_T1_Pounce", SFX_DASH_WHOOSH = "SFX_Goblin_Feastmaster_Whoosh_Quick", SFX_DASH_HIT = "SFX_Daggers_T2_Slash_Impact";
    private static final String SFX_REWIND = "SFX_Emit_Goblin_Winding", SFX_REWIND_TP = "SFX_Dungeon_Goblin_Portal_Void_Teleport_Cinematic", SFX_REWIND_TP_2D = "SFX_Portal_Neutral_Teleport_Local";
    private static final String SFX_RELEASE = "SFX_Emit_Dungeon_Goblin_Void_Strike", SFX_STRIKE = "SFX_Ability_Charged_Shot_Impact";
    private static final String SFX_SHATTER = "SFX_Crystal_Break", SFX_SHATTER_GLASS = "SFX_Glass_Break", SFX_SHATTER_2D = "SFX_Memories_Unlock_Local";

    /** Etat du lanceur a un instant (pour rejouer ses mouvements). */
    record Sample(long time, Vector3d pos, Rotation3f body, Rotation3f head, MovementStates states) {}

    /** Image du lanceur : porteur invisible habille de son modele, de son skin et de son etat de mouvement. */
    static final class Ghost {
        final Ref<EntityStore> ref;
        final long born, life;
        final Sample pose;
        boolean dressed;
        Ghost(Ref<EntityStore> ref, Sample pose, long born, long life) { this.ref = ref; this.pose = pose; this.born = born; this.life = life; }
    }

    static final class Stop {
        final Ref<EntityStore> caster;
        final Vector3d center, castPos;
        final float castHealth;
        final long startAt;
        final Map<Ref<EntityStore>, Float> stored = new HashMap<>();   // degats mis en reserve par cible figee
        final List<Ref<EntityStore>> cascade = new ArrayList<>();
        final List<Ghost> ghosts = new ArrayList<>();
        Sample trailFrom;                                               // pose ou la prochaine silhouette de trainee sera laissee
        Ref<EntityStore> clock, hands, hourglass;
        Model model;
        PlayerSkin skin;
        Set<Integer> seenPrimary;
        long lastPulse, lastGear, lastCrack, lastCascade, brokenAt, lastDash, lastFreeze, lastEdge, lastTick;
        boolean tock;
        boolean opened, stopped, rewinding, rewound, released, broken;
        Stop(Ref<EntityStore> caster, Vector3d center, Vector3d castPos, float castHealth, long startAt) {
            this.caster = caster; this.center = center; this.castPos = castPos; this.castHealth = castHealth; this.startAt = startAt;
        }
    }

    private static final Map<UUID, List<Stop>> stops = new ConcurrentHashMap<>();
    /** Cibles figees -> arret du temps qui les retient (lu par DivineDamageSystem). */
    private static final Map<Ref<EntityStore>, Stop> frozen = new ConcurrentHashMap<>();

    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        Vector3d pos = SpellFx.position(caster, store);
        if (uuid == null || pos == null) return;
        Vector3d center = SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK_DISTANCE);
        Stop s = new Stop(caster, center, pos, health(caster, store), System.currentTimeMillis());
        s.seenPrimary = SpellFx.chains(caster, store, InteractionType.Primary);
        ModelComponent mc = store.getComponent(caster, ModelComponent.getComponentType());
        PlayerSkinComponent skin = store.getComponent(caster, PlayerSkinComponent.getComponentType());
        s.model = mc != null ? mc.getModel() : null;
        s.skin = skin != null ? skin.getPlayerSkin() : null;
        stops.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(s);
    }

    private static float health(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            var map = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap.getComponentType());
            var v = map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
            return v != null ? v.get() : -1f;
        } catch (Exception e) {
            return -1f;
        }
    }

    /**
     * Arret du temps : un coup sur une cible figee est annule et mis en reserve. Retourne true si le coup doit etre
     * annule (appele par DivineDamageSystem).
     */
    public static boolean absorb(Ref<EntityStore> target, float amount) {
        if (frozen.isEmpty() || target == null) return false;
        Stop s = frozen.get(target);
        if (s == null || s.released) return false;
        s.stored.merge(target, amount, Float::sum);
        return true;
    }

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Stop> list = stops.get(uuid);
        if (list == null) return;
        long now = System.currentTimeMillis();
        for (Stop s : list) if (tickStop(caster, s, now, store, cb)) list.remove(s);
        if (list.isEmpty()) stops.remove(uuid);
    }

    private static boolean tickStop(Ref<EntityStore> caster, Stop s, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        long t = now - s.startAt;
        Vector3d casterPos = SpellFx.position(caster, store);
        Vector3d clockAt = new Vector3d(s.center).add(0, CLOCK_HEIGHT, 0);
        if (!s.opened) {
            s.opened = true;
            Vector3d x = new Vector3d(1, 0, 0);
            s.clock = SpellFx.spawnModel(caster, CLOCK_PROJECTILE, clockAt, x, cb);
            s.hands = SpellFx.spawnModel(caster, HANDS_PROJECTILE, clockAt, x, cb);
            if (casterPos != null) s.hourglass = SpellFx.spawnModel(caster, HOURGLASS_PROJECTILE, new Vector3d(casterPos).add(0, HOURGLASS_HEIGHT, 0), x, cb);
            SpellFx.fx(FX_GEAR, clockAt, 10.0f, store, cb);
            SpellFx.sound(SFX_CLOCK, s.center, cb);
            SpellFx.sound(SFX_CLOCK_OPEN, s.center, cb);
            SpellFx.sound2dNear(SFX_CLOCK_2D, s.center, RADIUS * 2, store);
        }
        SpellFx.hold(s.clock, clockAt, null, store);
        SpellFx.hold(s.hands, clockAt, null, store);
        if (casterPos != null) SpellFx.hold(s.hourglass, new Vector3d(casterPos).add(0, HOURGLASS_HEIGHT + Math.sin(now / 400.0) * 0.08, 0), null, store);
        if (casterPos != null && !s.rewinding) trail(caster, s, casterPos, now, store, cb);
        tickGhosts(s, now, store, cb);
        // Arret du temps (et toute creature qui entre dans la zone pendant l'arret)
        if (!s.stopped && t >= STOP_AT) {
            s.stopped = true;
            freezeNewcomers(caster, s, now, store, cb);
            for (double r = 5; r <= RADIUS; r += 10) {                                       // onde verte qui fige la zone
                for (int k = 0; k < 12; k++) {
                    double a = Math.PI * 2 * k / 12 + r;
                    SpellFx.fx(FX_TIME, new Vector3d(s.center).add(Math.cos(a) * r, 1, Math.sin(a) * r), 2.5f, store, cb);
                }
            }
            SpellFx.fx(FX_TIME, new Vector3d(s.center).add(0, 1, 0), 8.0f, store, cb);
            SpellFx.shake(s.center, RADIUS * 2, "Impact_Light", store);
            SpellFx.sound2dNear(SFX_STOP_2D, s.center, RADIUS * 2, store);
            SpellFx.sound(SFX_STOP_CRACK, s.center, cb);
            SpellFx.sound(SFX_STOP_GEARS, s.center, cb);
        }
        long stopEnd = STOP_AT + STOP_MS;
        if (s.stopped && !s.rewinding && now - s.lastFreeze >= 1_000) freezeNewcomers(caster, s, now, store, cb);
        // Tic-tac de l'horloge pendant l'arret du temps (au lanceur et au centre)
        if (s.stopped && !s.rewinding && now - s.lastTick >= 1_000) {
            s.lastTick = now;
            s.tock = !s.tock;
            String tick = s.tock ? SFX_TOCK : SFX_TICK;
            SpellFx.sound(tick, s.center, cb);
            if (casterPos != null) SpellFx.sound(tick, casterPos, cb);
        }
        // Pendant le sort : dash temporel a chaque attaque
        if (!s.rewinding && casterPos != null) {
            Set<Integer> primary = SpellFx.chains(caster, store, InteractionType.Primary);
            boolean attacked = false;
            for (Integer id : primary) if (!s.seenPrimary.contains(id)) attacked = true;
            s.seenPrimary = primary;
            if (attacked && now - s.lastDash >= DASH_COOLDOWN_MS) dash(caster, s, casterPos, now, store, cb);
        }
        // Fissures temporelles sur les ennemis figes
        if (s.stopped && !s.broken && now - s.lastCrack >= 1000) {
            s.lastCrack = now;
            for (Ref<EntityStore> e : s.stored.keySet()) {
                Vector3d p = e.isValid() ? SpellFx.position(e, store) : null;
                if (p != null) SpellFx.fx(FX_TIME, new Vector3d(p).add(0, 1.2, 0), 0.6f, store, cb);
            }
        }
        // 1. Retour en arriere : aiguilles a rebours, trainee temporelle, puis teleportation
        if (!s.rewinding && t >= stopEnd) {
            s.rewinding = true;
            SpellFx.remove(s.hands, cb);
            s.hands = SpellFx.spawnModel(caster, REWIND_HANDS_PROJECTILE, clockAt, new Vector3d(1, 0, 0), cb);
            if (casterPos != null) {
                Vector3d from = new Vector3d(casterPos).add(0, 1, 0), to = new Vector3d(s.castPos).add(0, 1, 0);
                int n = (int) Math.min(30, Math.max(2, from.distance(to) / 1.5));
                for (int i = 0; i <= n; i++) SpellFx.fx(FX_TIME, new Vector3d(from).lerp(to, i / (double) n), 0.7f, store, cb);
                spawnGhost(caster, s, sample(caster, casterPos, now, store), now, GHOST_MS, cb);   // silhouette laissee sur place
                SpellFx.sound(SFX_REWIND, casterPos, cb);
            }
            SpellFx.sound(SFX_REWIND, s.center, cb);
        }
        if (s.rewinding && !s.rewound && t >= stopEnd + REWIND_TELEPORT_MS) {
            s.rewound = true;
            rewind(caster, s, store, cb);
        }
        // 2. Les degats en reserve s'abattent en cascade, ennemi par ennemi
        if (s.rewound && !s.released && t >= stopEnd + REWIND_TELEPORT_MS + CASCADE_DELAY_MS) {
            if (s.cascade.isEmpty() && s.lastCascade == 0) {
                s.cascade.addAll(s.stored.keySet());
                SpellFx.fx(FX_GEAR, clockAt, 12.0f, store, cb);
                SpellFx.sound(SFX_RELEASE, s.center, cb);
                SpellFx.sound(SFX_STOP_GEARS, s.center, cb);
                SpellFx.shake(s.center, RADIUS * 3, "Impact_Strong", store);
            }
            if (now - s.lastCascade >= CASCADE_STEP_MS) {
                s.lastCascade = now;
                if (s.cascade.isEmpty()) { s.released = true; }
                else strike(caster, s, s.cascade.remove(0), store, cb);
            }
        }
        // 3. Le temps se brise : les ennemis sont enfin liberes
        if (s.released && !s.broken && now - s.lastCascade >= BREAK_DELAY_MS) {
            s.broken = true;
            s.brokenAt = now;
            shatter(s, store, cb);
        }
        // Engrenages qui tournoient dans la zone, et bord de la zone marque d'etincelles vertes
        if (now - s.lastGear >= 700 && !s.broken) {
            s.lastGear = now;
            double a = now / 300.0;
            SpellFx.fx(FX_GEAR, new Vector3d(s.center).add(Math.cos(a) * RADIUS * 0.6, 3 + Math.sin(a * 0.7), Math.sin(a) * RADIUS * 0.6), 1.5f, store, cb);
        }
        if (now - s.lastEdge >= 2_000 && !s.broken) {
            s.lastEdge = now;
            double off = (now / 2_000) * 0.2;
            for (int k = 0; k < 20; k++) {
                double a = off + Math.PI * 2 * k / 20;
                SpellFx.fx(FX_GEAR, SpellFx.ground(SpellFx.world(store), new Vector3d(s.center).add(Math.cos(a) * RADIUS, 0, Math.sin(a) * RADIUS)).add(0, 1, 0), 1.2f, store, cb);
            }
        }
        // Allies : acceleration du temps
        if (now - s.lastPulse >= 1000 && !s.broken) {
            s.lastPulse = now;
            for (Ref<EntityStore> ally : SpellFx.alliesAround(caster, s.center, RADIUS, store)) {
                SpellFx.effect(ally, HASTE_EFFECT, cb);
                SpellFx.restore(ally, DefaultEntityStatTypes.getStamina(), STAMINA, cb);
            }
        }
        if (s.broken && now - s.brokenAt >= LINGER_MS) {
            for (Ref<EntityStore> m : new Ref[]{s.clock, s.hands, s.hourglass}) {
                Vector3d p = SpellFx.position(m, store);
                if (p != null) SpellFx.fx(FX_GEAR, p, 4.0f, store, cb);
                SpellFx.remove(m, cb);
            }
            for (Ghost gh : s.ghosts) SpellFx.remove(gh.ref, cb);
            s.ghosts.clear();
            return true;
        }
        return false;
    }

    /** Fige les ennemis de la zone qui ne le sont pas encore (au debut, puis chaque seconde pour ceux qui entrent). */
    private static void freezeNewcomers(Ref<EntityStore> caster, Stop s, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        s.lastFreeze = now;
        long remaining = Math.max(1_000, s.startAt + STOP_AT + STOP_MS - now);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, s.center, RADIUS, store)) {
            if (s.stored.containsKey(e)) continue;
            frozen.put(e, s);
            s.stored.put(e, 0f);
            SpellFx.lockInPlace(e, remaining + 4_000, store);           // libere a la brisure du temps
            SpellFx.effect(e, FROZEN_EFFECT, cb);
            Vector3d p = SpellFx.position(e, store);
            if (p != null) {
                SpellFx.fx(FX_TIME, new Vector3d(p).add(0, 1, 0), 1.8f, store, cb);
                SpellFx.sound(SFX_STOP_CRACK, p, cb);
            }
        }
    }

    // ===================== SILHOUETTES REMANENTES ET DASH TEMPOREL =====================

    /** Etat courant du lanceur : position, corps, tete, etat de mouvement. */
    private static Sample sample(Ref<EntityStore> caster, Vector3d pos, long now, Store<EntityStore> store) {
        TransformComponent t = store.getComponent(caster, TransformComponent.getComponentType());
        HeadRotation head = store.getComponent(caster, HeadRotation.getComponentType());
        MovementStatesComponent ms = store.getComponent(caster, MovementStatesComponent.getComponentType());
        return new Sample(now, new Vector3d(pos), t != null ? new Rotation3f(t.getRotation()) : new Rotation3f(),
                head != null ? new Rotation3f(head.getRotation()) : null,
                ms != null && ms.getMovementStates() != null ? new MovementStates(ms.getMovementStates()) : null);
    }

    /** Habille un porteur : modele, skin et etat de mouvement du lanceur, voile vert (effet). */
    private static void dress(Stop s, Ghost gh, CommandBuffer<EntityStore> cb) {
        gh.dressed = true;
        if (s.model != null) cb.putComponent(gh.ref, ModelComponent.getComponentType(), new ModelComponent(s.model));
        if (s.skin != null) cb.putComponent(gh.ref, PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(s.skin));
        MovementStatesComponent ms = new MovementStatesComponent();
        if (gh.pose != null && gh.pose.states() != null) ms.setMovementStates(new MovementStates(gh.pose.states()));
        cb.putComponent(gh.ref, MovementStatesComponent.getComponentType(), ms);
        if (gh.pose != null && gh.pose.head() != null) cb.putComponent(gh.ref, HeadRotation.getComponentType(), new HeadRotation(gh.pose.head()));
        SpellFx.effect(gh.ref, GHOST_EFFECT, cb);
    }

    /** Pose le porteur : position, corps, tete et etat de mouvement de l'instant rejoue. */
    private static void pose(Ghost gh, Sample p, Store<EntityStore> store) {
        SpellFx.hold(gh.ref, p.pos(), p.body(), store);
        if (p.head() != null) {
            HeadRotation head = store.getComponent(gh.ref, HeadRotation.getComponentType());
            if (head != null) head.setRotation(p.head());
        }
        if (p.states() != null) {
            MovementStatesComponent ms = store.getComponent(gh.ref, MovementStatesComponent.getComponentType());
            if (ms != null && !p.states().equals(ms.getMovementStates())) ms.setMovementStates(new MovementStates(p.states()));
        }
    }

    /**
     * Trainee d'images remanentes : des que le lanceur s'est deplace de TRAIL_STEP blocs, une silhouette figee est
     * laissee la ou il se trouvait (un pas en arriere, pour ne pas se superposer a lui). A l'arret, rien n'est seme.
     */
    private static void trail(Ref<EntityStore> caster, Stop s, Vector3d pos, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (s.trailFrom == null) { s.trailFrom = sample(caster, pos, now, store); return; }
        double moved = pos.distance(s.trailFrom.pos());
        if (moved > TRAIL_TELEPORT) { s.trailFrom = sample(caster, pos, now, store); return; }
        if (moved < TRAIL_STEP || now - s.trailFrom.time() < TRAIL_MIN_MS) return;
        int trail = 0;                                                    // la plus ancienne s'efface au-dela de TRAIL_COUNT
        for (int i = s.ghosts.size() - 1; i >= 0; i--) {
            if (s.ghosts.get(i).life != TRAIL_GHOST_MS || ++trail < TRAIL_COUNT) continue;
            SpellFx.remove(s.ghosts.remove(i).ref, cb);
        }
        spawnGhost(caster, s, s.trailFrom, now, TRAIL_GHOST_MS, cb);
        s.trailFrom = sample(caster, pos, now, store);
    }

    /** Silhouette figee (trainee, dash, retour en arriere) : copie du lanceur a un instant, qui s'efface apres `life` ms. */
    private static void spawnGhost(Ref<EntityStore> caster, Stop s, Sample pose, long now, long life, CommandBuffer<EntityStore> cb) {
        if (s.ghosts.size() >= GHOST_MAX) SpellFx.remove(s.ghosts.remove(0).ref, cb);
        Ref<EntityStore> ref = SpellFx.spawnModel(caster, GHOST_PROJECTILE, pose.pos(), new Vector3d(1, 0, 0), cb);
        if (ref != null) s.ghosts.add(new Ghost(ref, pose, now, life));
    }

    private static void tickGhosts(Stop s, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        for (int i = s.ghosts.size() - 1; i >= 0; i--) {
            Ghost gh = s.ghosts.get(i);
            if (now - gh.born >= gh.life) { SpellFx.remove(gh.ref, cb); s.ghosts.remove(i); continue; }
            if (!gh.ref.isValid()) continue;
            if (!gh.dressed) { dress(s, gh, cb); SpellFx.fx(FX_AFTERIMAGE, gh.pose.pos(), gh.life < GHOST_MS ? 0.6f : 1.0f, store, cb); }
            pose(gh, gh.pose, store);
        }
    }

    /** Dash temporel : le lanceur apparait devant l'ennemi qu'il vise et le frappe, en laissant des silhouettes. */
    private static void dash(Ref<EntityStore> caster, Stop s, Vector3d from, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d look = SpellFx.lookDirection(caster, store);
        Ref<EntityStore> best = null;
        double bestScore = -1;
        Vector3d bestPos = null;
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, from, DASH_RANGE, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            Vector3d to = new Vector3d(p).add(0, 1, 0).sub(new Vector3d(from).add(0, 1.6, 0));
            double dist = to.length();
            if (dist < 0.5) continue;
            double align = to.div(dist).dot(look);
            if (align < DASH_MIN_ALIGN) continue;
            double score = align - dist / DASH_RANGE * 0.35;
            if (score > bestScore) { bestScore = score; best = e; bestPos = p; }
        }
        if (best == null) return;
        s.lastDash = now;
        Vector3d dir = new Vector3d(bestPos.x - from.x, 0, bestPos.z - from.z);
        if (dir.lengthSquared() < 1e-6) dir.set(look.x, 0, look.z);
        double dist = dir.length();
        dir.normalize();
        if (dist <= DASH_STOP + 0.3) {                                                      // deja au contact : frappe sur place
            hit(caster, best, bestPos, store, cb);
            return;
        }
        Vector3d land = new Vector3d(bestPos).sub(new Vector3d(dir).mul(DASH_STOP));
        land.y = bestPos.y;
        Rotation3f facing = SpellFx.facing(dir, false);
        Sample start = sample(caster, from, now, store);
        for (double f : new double[]{0.0, 0.35, 0.7}) {                                     // silhouettes le long du dash
            spawnGhost(caster, s, new Sample(now, new Vector3d(from).lerp(land, f), facing, facing, start.states()), now, GHOST_MS, cb);
        }
        int n = (int) Math.max(2, from.distance(land) / 1.2);
        for (int i = 0; i <= n; i++) SpellFx.fx(FX_DASH, new Vector3d(from).lerp(land, i / (double) n), 1.0f, store, cb);
        SpellFx.teleport(caster, land, facing, store, cb);
        SpellFx.protectFromFall(caster, 1_500, store);
        SpellFx.sound(SFX_DASH, from, cb);
        SpellFx.sound(SFX_DASH_WHOOSH, land, cb);
        hit(caster, best, bestPos, store, cb);
    }

    private static void hit(Ref<EntityStore> caster, Ref<EntityStore> target, Vector3d at, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.damage(caster, target, "Physical", DASH_DAMAGE, null, "Impact_Light", cb);   // sur une cible figee : mis en reserve
        SpellFx.fx(FX_TIME, new Vector3d(at).add(0, 1, 0), 1.6f, store, cb);
        SpellFx.sound(SFX_DASH_HIT, at, cb);
    }

    // ===================== FIN DU SORT =====================

    /** Degats en reserve d'une cible (x RELEASE_MULTIPLIER + onde temporelle) ; elle reste figee jusqu'a la brisure. */
    private static void strike(Ref<EntityStore> caster, Stop s, Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        frozen.remove(target, s);                                         // ce coup-ci ne doit plus etre absorbe
        if (!target.isValid()) return;
        float total = s.stored.getOrDefault(target, 0f) * RELEASE_MULTIPLIER + TIME_SHOCK_DAMAGE;
        SpellFx.damage(caster, target, "Physical", total, null, "Impact_Strong", cb);
        Vector3d p = SpellFx.position(target, store);
        if (p != null) {
            SpellFx.fx(FX_TIME, new Vector3d(p).add(0, 1, 0), 2.5f, store, cb);
            SpellFx.sound(SFX_STRIKE, p, cb);
        }
    }

    /** Eclat de temps : le temps se brise en eclats verts, la stase est levee, les creatures sont liberees. */
    private static void shatter(Stop s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx(FX_SHATTER, new Vector3d(s.center).add(0, 1, 0), 6.0f, store, cb);
        SpellFx.fx(FX_SHATTER, new Vector3d(s.center).add(0, CLOCK_HEIGHT, 0), 12.0f, store, cb);
        SpellFx.fx(FX_TIME, new Vector3d(s.center).add(0, 1, 0), 10.0f, store, cb);
        SpellFx.sound(SFX_SHATTER, s.center, cb);
        SpellFx.sound(SFX_SHATTER_GLASS, s.center, cb);
        SpellFx.sound2dNear(SFX_SHATTER_2D, s.center, RADIUS * 2, store);
        for (Ref<EntityStore> e : s.stored.keySet()) {
            frozen.remove(e, s);
            SpellFx.unlock(e);
            if (!e.isValid()) continue;
            com.eldanior.system.config.Effects.EffectsManager.removeEffect(e, FROZEN_EFFECT, cb);
            Vector3d p = SpellFx.position(e, store);
            if (p != null) {
                SpellFx.fx(FX_SHATTER, new Vector3d(p), 1.4f, store, cb);
                SpellFx.sound(SFX_SHATTER, p, cb);
            }
        }
    }

    /** Retour en arriere : position et vie du lancement. */
    private static void rewind(Ref<EntityStore> caster, Stop s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d now = SpellFx.position(caster, store);
        if (now == null || caster.getStore() != store) return;
        float current = health(caster, store);
        if (s.castHealth > 0 && current >= 0 && current < s.castHealth) {
            try {
                var map = store.getComponent(caster, com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap.getComponentType());
                if (map != null) map.addStatValue(DefaultEntityStatTypes.getHealth(), s.castHealth - current);
            } catch (Exception ignored) {}
        }
        if (now.distance(s.castPos) <= REWIND_MAX_DISTANCE) {
            SpellFx.teleport(caster, s.castPos, store, cb);
            SpellFx.protectFromFall(caster, 2_000, store);
            SpellFx.fx(FX_TIME, new Vector3d(s.castPos).add(0, 1, 0), 3.5f, store, cb);
            SpellFx.sound(SFX_REWIND_TP, s.castPos, cb);
            SpellFx.sound2dNear(SFX_REWIND_TP_2D, s.castPos, 24, store);
        }
    }

    public static void handleDisconnect(UUID uuid) { stops.remove(uuid); }
}
