package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gilgamesh Babylon — sort divin de lumiere (Rune_GilgameshBabylon) : la Porte de Babylone, le tresor du roi.
 *
 * Ouvre {@link #WEAPON_COUNT} portails dores ondulants, repartis en eventail dans le dos du lanceur (ils le suivent).
 * De chacun sort lentement une arme de lumiere (epee, katana, epee ornementee, espadon), lame en avant, a moitie
 * engagee dans le portail (la moitie cote poignee est masquee tant que l'arme n'est pas lancee), pointee vers ce que vise
 * le lanceur. Le lanceur devient entierement dore (descente de Gilgamesh). Un halo d'ange 3D flotte au-dessus de sa tete et une poussiere
 * doree s'eleve autour de lui.
 *
 *  - Chaque attaque (clic gauche = nouvelle chaine d'interaction "Primary") lance une arme vers le point vise ;
 *    elle vole pointe en avant et devie legerement vers l'ennemi le plus proche du viseur.
 *  - Relancer le sort pendant qu'il reste des armes declenche la salve finale « Enuma Elish » : toutes les armes
 *    restantes partent en rafale et explosent en lumiere a l'impact (degats de zone).
 *  - Une arme qui touche reste plantee dans sa cible (ou dans le sol) 2 s, puis se dissout en poussiere doree.
 * Le sort reste actif tant qu'il reste des armes a lancer.
 */
public final class GilgameshBabylon {

    private GilgameshBabylon() {}

    // ===== Reglages =====
    public static final int WEAPON_COUNT = 30;
    private static final long SUMMON_INTERVAL_MS = 60;    // apparition une par une
    private static final long LAUNCH_COOLDOWN_MS = 120;   // cadence max de tir
    private static final long SALVO_INTERVAL_MS = 45;     // cadence de la salve finale
    private static final double AIM_RANGE = 60.0;
    static final float WEAPON_DAMAGE = 500f;
    private static final float SALVO_BLAST_DAMAGE = 300f; // explosion de lumiere des armes de la salve
    private static final double SALVO_BLAST_RADIUS = 3.5;
    private static final double HIT_MARGIN = 0.45;         // marge autour de la boite de l'ennemi pour le contact
    private static final double LOOK_AHEAD_S = 0.07;       // le contact est teste jusqu'a la position du tick suivant
    private static final long FLIGHT_TIMEOUT_MS = 3000;
    private static final long PLANTED_MS = 2000;           // arme plantee avant de se dissoudre
    private static final double HOMING_CONE = Math.toRadians(35); // cible au lancement : ennemi dans ce cone autour du viseur
    private static final double HOMING_REACQUIRE_CONE = Math.toRadians(40); // en vol : nouvelle cible devant l'arme
    private static final double HOMING_REACQUIRE_RANGE = 30.0;
    private static final double HOMING_STRENGTH = 0.35;   // part de la direction corrigee a chaque tick
    // Porte de Babylone : portails repartis dans une ellipse verticale derriere le lanceur
    private static final double GATE_HALF_WIDTH = 11.0;    // demi-largeur de l'eventail de portails (~2.3 blocs entre portails)
    private static final double GATE_HALF_HEIGHT = 4.6;    // demi-hauteur
    private static final double GATE_CENTER_Y = 6.0;       // hauteur du centre (le portail le plus bas reste vers la tete)
    private static final double GATE_BACK = 2.0;           // recul derriere le dos (+ jusqu'a 0.8 selon le portail) : les armes (~2 blocs) ne touchent pas le lanceur
    private static final long EMERGE_MS = 400;             // duree de la sortie de l'arme
    private static final double HALO_HEIGHT = 2.35;        // halo d'ange au-dessus de la tete du lanceur
    private static final long AURA_EVERY_MS = 500;

    // ===== Assets =====
    private static final String[] WEAPONS = {"Sword", "Katana", "Ornate", "Longsword"}; // armes droites (axe = projectile)
    /**
     * Distance (blocs) entre le portail et l'origine du projectile de chaque arme : en position finale, le portail est
     * exactement au debut de la partie visible (la poignee, ~35 %, est masquee) ; la sortie commence pointe au ras du
     * portail. Calcule par scripts/rune_aura_icons/gen_gilgamesh_halves.py (modeles d'entites : 64 unites = 1 bloc).
     */
    private static final double[] EMERGE_FINAL = {1.68, 1.26, 1.59, 1.69};
    private static final double[] EMERGE_START = {0.34, -0.06, 0.10, 0.29};
    private static final String GOLDEN_EFFECT = "Gilgamesh_Roi_Dore";      // descente de Gilgamesh : lanceur entierement dore // Projectile_Config_Gilgamesh_<arme>[_Hover]
    private static final String PORTAL_PROJECTILE = "Projectile_Config_Gilgamesh_Portal";
    private static final String HALO_PROJECTILE = "Projectile_Config_Gilgamesh_Halo";
    private static final String FX_SUMMON = "Gilgamesh_Sword_Burst";
    private static final String FX_HIT = "Gilgamesh_Sword_Burst";
    private static final String FX_AURA = "Gilgamesh_King_Aura";
    private static final String SFX_SUMMON = "SFX_Deployable_Totem_Heal_Spawn";
    private static final String SFX_LAUNCH = "SFX_Ability_Wind_Strike_Slash";
    private static final String SFX_HIT = "SFX_Staff_Flame_Fireball_Impact";
    private static final String SFX_SALVO = "SFX_Golem_Frost_Slam_Impact";

    /** Arme en attente dans son portail (emplacement fixe dans l'eventail). */
    static final class Hover {
        final Ref<EntityStore> weapon, portal;
        final int type, slot;
        final long summonedAt;
        Hover(Ref<EntityStore> weapon, Ref<EntityStore> portal, int type, int slot, long summonedAt) {
            this.weapon = weapon; this.portal = portal; this.type = type; this.slot = slot; this.summonedAt = summonedAt;
        }
    }

    static final class Flying {
        final Ref<EntityStore> ref, owner;
        final long launchedAt;
        final boolean salvo;
        Ref<EntityStore> target;         // visee assistee (peut etre null, re-acquise en vol)
        Vector3d lastPos, lastVel;
        boolean spawned;                 // l'entite a ete creee (la creation via le CommandBuffer prend un tick)
        Flying(Ref<EntityStore> ref, Ref<EntityStore> owner, long launchedAt, boolean salvo, Ref<EntityStore> target) {
            this.ref = ref; this.owner = owner; this.launchedAt = launchedAt; this.salvo = salvo; this.target = target;
        }
    }

    /** Arme plantee dans une cible (suit sa position) ou dans le sol, jusqu'a sa dissolution. */
    static final class Planted {
        final Ref<EntityStore> ref, target;
        final Vector3d offset;           // position relative a la cible (ou absolue sans cible)
        final Rotation3f rotation;
        final long until;
        Planted(Ref<EntityStore> ref, Ref<EntityStore> target, Vector3d offset, Rotation3f rotation, long until) {
            this.ref = ref; this.target = target; this.offset = offset; this.rotation = rotation; this.until = until;
        }
    }

    static final class Volley {
        long startAt;
        final List<Hover> hovering = new ArrayList<>();
        final List<Flying> flying = new ArrayList<>();
        final List<Planted> planted = new ArrayList<>();
        Ref<EntityStore> halo;
        long lastHaloSpawn, lastAura, lastLaunch;
        int summoned, launched;
        boolean salvo;
        Volley(long startAt) { this.startAt = startAt; }
    }

    private static final Map<UUID, Volley> volleys = new ConcurrentHashMap<>();
    /** Chaines d'attaque (Primary) deja comptees, par lanceur : chaque nouvelle chaine = un tir. */
    private static final Map<UUID, Set<Integer>> seenAttacks = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> pendingLaunches = new ConcurrentHashMap<>();

    /**
     * Ouvre la Porte de Babylone. S'il reste des armes en attente : salve finale « Enuma Elish ».
     * Si toutes ont deja ete lancees : nouvelle invocation complete.
     */
    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        if (uuid == null) return;
        long now = System.currentTimeMillis();
        Volley v = volleys.get(uuid);
        seenAttacks.put(uuid, currentAttackChains(caster, store)); // les attaques deja en cours ne comptent pas
        if (v == null) {
            volleys.put(uuid, new Volley(now));
        } else if (!v.hovering.isEmpty()) {
            v.salvo = true;
            Vector3d pos = SpellFx.position(caster, store);
            if (pos != null) {
                SpellFx.fx(FX_HIT, new Vector3d(pos).add(0, GATE_CENTER_Y, 0), 3.0f, store, store);
                SpellFx.sound(SFX_SALVO, pos, store);
            }
        } else if (v.summoned >= WEAPON_COUNT) {
            v.startAt = now;
            v.summoned = 0;
        }
    }

    /** Chaines d'interaction "Primary" (attaques) actuellement actives sur le lanceur. */
    private static Set<Integer> currentAttackChains(Ref<EntityStore> caster, Store<EntityStore> store) {
        Set<Integer> ids = new HashSet<>();
        try {
            InteractionManager manager = store.getComponent(caster, InteractionModule.get().getInteractionManagerComponent());
            if (manager == null) return ids;
            for (InteractionChain chain : manager.getChains().values()) {
                if (chain != null && chain.getType() == InteractionType.Primary) ids.add(chain.getChainId());
            }
        } catch (Exception e) {
            EldaniorLogger.error("GilgameshBabylon: lecture des attaques", e);
        }
        return ids;
    }

    /** Nombre de nouvelles attaques depuis le dernier tick. */
    private static int newAttacks(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store) {
        Set<Integer> current = currentAttackChains(caster, store);
        Set<Integer> seen = seenAttacks.getOrDefault(uuid, Set.of());
        int count = 0;
        for (Integer id : current) if (!seen.contains(id)) count++;
        seenAttacks.put(uuid, current);
        return count;
    }

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Volley v = volleys.get(uuid);
        if (v == null) return;
        long now = System.currentTimeMillis();
        Vector3d casterPos = SpellFx.position(caster, store);
        if (casterPos == null) return;
        Vector3d look = SpellFx.lookDirection(caster, store);
        boolean active = !v.hovering.isEmpty() || v.summoned < WEAPON_COUNT;

        // 0. Halo d'ange au-dessus de la tete et aura du roi
        if (v.halo == null || !v.halo.isValid()) {
            if (now - v.lastHaloSpawn >= 1000) {              // absent ou retire par le moteur : on le repose
                v.lastHaloSpawn = now;
                v.halo = spawn(caster, HALO_PROJECTILE, new Vector3d(casterPos).add(0, HALO_HEIGHT, 0), new Vector3d(1, 0, 0), cb);
            }
        } else {
            place(v.halo, new Vector3d(casterPos).add(0, HALO_HEIGHT + Math.sin(now / 350.0) * 0.06, 0), null, store); // leger flottement
        }
        if (active && now - v.lastAura >= AURA_EVERY_MS) {
            v.lastAura = now;
            SpellFx.fx(FX_AURA, new Vector3d(casterPos).add(0, 0.1, 0), 2.0f, store, cb);
            SpellFx.effect(caster, GOLDEN_EFFECT, cb);
        }

        Vector3d aim = (v.hovering.isEmpty() && v.summoned >= WEAPON_COUNT) ? null : aimPoint(caster, casterPos, store);

        // 1. Ouverture progressive des portails ; l'arme apparait dans le portail
        while (v.summoned < WEAPON_COUNT && now - v.startAt >= v.summoned * SUMMON_INTERVAL_MS) {
            int slot = v.summoned, type = slot % WEAPONS.length;
            Vector3d gate = gateSlot(casterPos, look, slot);
            Vector3d dir = aimDir(gate, aim, look);
            Ref<EntityStore> portal = spawn(caster, PORTAL_PROJECTILE, gate, dir, cb);
            Ref<EntityStore> weapon = spawn(caster, hoverConfig(type), gate, dir, cb);
            if (weapon != null) v.hovering.add(new Hover(weapon, portal, type, slot, now));
            SpellFx.fx(FX_SUMMON, gate, 0.6f, store, cb);
            if (v.summoned % 4 == 0) SpellFx.sound(SFX_SUMMON, gate, cb);
            v.summoned++;
        }

        // 2. Portails fixes dans le dos du lanceur, face a la cible ; chaque arme en sort lame en avant, poignee dans le portail
        for (Hover h : v.hovering) {
            Vector3d gate = gateSlot(casterPos, look, h.slot);
            Vector3d dir = aimDir(gate, aim, look);
            double t = Math.min(1.0, (now - h.summonedAt) / (double) EMERGE_MS);
            double out = EMERGE_START[h.type] + (EMERGE_FINAL[h.type] - EMERGE_START[h.type]) * t;
            Rotation3f rot = pointing(dir);
            place(h.portal, gate, rot, store);
            place(h.weapon, new Vector3d(gate).add(new Vector3d(dir).mul(out)), rot, store);
        }

        // 3. Tir : une arme par attaque (clic gauche), ou toutes en rafale pendant la salve finale
        int pending = pendingLaunches.getOrDefault(uuid, 0) + newAttacks(uuid, caster, store);
        if (v.salvo) {
            pending = 0;
            if (!v.hovering.isEmpty() && now - v.lastLaunch >= SALVO_INTERVAL_MS) {
                v.lastLaunch = now;
                launch(caster, v, casterPos, store, cb, now, true);
            }
            if (v.hovering.isEmpty()) v.salvo = false;
        } else if (pending > 0 && !v.hovering.isEmpty() && now - v.lastLaunch >= LAUNCH_COOLDOWN_MS) {
            pending--;
            v.lastLaunch = now;
            launch(caster, v, casterPos, store, cb, now, false);
        }
        pendingLaunches.put(uuid, Math.min(pending, 5));

        // 4. Armes en vol (contact, arret, delai) puis armes plantees
        v.flying.removeIf(f -> tickFlying(caster, v, f, now, store, cb));
        v.planted.removeIf(p -> tickPlanted(p, now, store, cb));

        // Fin du sort : toutes les armes lancees, retombees et dissoutes
        if (!active && v.flying.isEmpty() && v.planted.isEmpty()) {
            if (v.halo != null && v.halo.isValid()) cb.tryRemoveEntity(v.halo, RemoveReason.REMOVE);
            volleys.remove(uuid);
            pendingLaunches.remove(uuid);
            seenAttacks.remove(uuid);
        }
    }

    private static String hoverConfig(int type) { return "Projectile_Config_Gilgamesh_" + WEAPONS[type] + "_Hover"; }
    private static String flyingConfig(int type) { return "Projectile_Config_Gilgamesh_" + WEAPONS[type]; }

    /** Deplace une entite immobile (position, orientation optionnelle) et annule son elan. */
    private static void place(Ref<EntityStore> ref, Vector3d pos, Rotation3f rot, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) return;
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (t == null) return;
        t.setPosition(pos);
        if (rot != null) t.setRotation(rot);
        Velocity vel = store.getComponent(ref, Velocity.getComponentType());
        if (vel != null) vel.setZero();
    }

    /**
     * Position fixe du portail `slot` : repartition en spirale (angle d'or) dans une ellipse verticale derriere le
     * lanceur, orientee selon son regard horizontal. Les portails restent en place quand d'autres se referment.
     */
    private static Vector3d gateSlot(Vector3d casterPos, Vector3d look, int slot) {
        Vector3d fwd = new Vector3d(look.x, 0, look.z);
        if (fwd.lengthSquared() < 1e-6) fwd.set(0, 0, 1);
        fwd.normalize();
        Vector3d right = new Vector3d(-fwd.z, 0, fwd.x);
        double r = Math.sqrt((slot + 0.5) / WEAPON_COUNT);
        double a = slot * 2.399963;                          // angle d'or : repartition reguliere sans motif
        double back = GATE_BACK + 0.8 * ((slot * 0.618034) % 1.0);
        return new Vector3d(casterPos)
                .add(new Vector3d(right).mul(Math.cos(a) * r * GATE_HALF_WIDTH))
                .add(0, GATE_CENTER_Y + Math.sin(a) * r * GATE_HALF_HEIGHT, 0)
                .sub(new Vector3d(fwd).mul(back));
    }

    /** Direction portail -> point vise (le regard si aucun point). */
    private static Vector3d aimDir(Vector3d gate, Vector3d aim, Vector3d look) {
        Vector3d dir = aim != null ? new Vector3d(aim).sub(gate) : new Vector3d(look);
        if (dir.lengthSquared() < 1e-6) dir.set(look);
        return dir.normalize();
    }

    /** Orientation d'un projectile pointe vers `dir` (meme calcul que le moteur au lancement). */
    private static Rotation3f pointing(Vector3d dir) {
        Rotation3f rot = new Rotation3f();
        rot.setYaw(PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(dir.x, dir.z)));
        rot.setPitch(PhysicsMath.pitchFromDirection(dir.x, dir.y, dir.z));
        return rot;
    }

    private static void launch(Ref<EntityStore> caster, Volley v, Vector3d casterPos, Store<EntityStore> store,
                               CommandBuffer<EntityStore> cb, long now, boolean salvo) {
        Hover h = v.hovering.remove(0);
        Vector3d from = h.weapon != null && h.weapon.isValid() ? SpellFx.position(h.weapon, store) : null;
        if (from == null) from = new Vector3d(casterPos).add(0, GATE_CENTER_Y, 0);
        if (h.weapon != null && h.weapon.isValid()) cb.tryRemoveEntity(h.weapon, RemoveReason.REMOVE);
        if (h.portal != null && h.portal.isValid()) {
            Vector3d p = SpellFx.position(h.portal, store);
            if (p != null) SpellFx.fx(FX_SUMMON, p, 0.7f, store, cb);   // le portail se referme dans un eclat
            cb.tryRemoveEntity(h.portal, RemoveReason.REMOVE);
        }

        Vector3d aim = aimPoint(caster, casterPos, store);
        Ref<EntityStore> target = homingTarget(caster, casterPos, store);
        Vector3d targetPos = target != null ? center(target, store) : null;
        if (targetPos != null) aim = targetPos;
        Vector3d dir = new Vector3d(aim).sub(from);
        if (dir.lengthSquared() < 1e-6) dir = SpellFx.lookDirection(caster, store);
        dir.normalize();
        Ref<EntityStore> weapon = spawn(caster, flyingConfig(h.type), from, dir, cb);
        if (weapon != null) {
            Flying f = new Flying(weapon, caster, now, salvo, target);
            f.lastPos = new Vector3d(from);
            v.flying.add(f);
        }
        v.launched++;
        SpellFx.sound(SFX_LAUNCH, from, cb);
    }

    /** Point vise : bloc dans la ligne de mire, sinon AIM_RANGE blocs devant les yeux. */
    private static Vector3d aimPoint(Ref<EntityStore> caster, Vector3d casterPos, Store<EntityStore> store) {
        Vector3i block = TargetUtil.getTargetBlock(caster, AIM_RANGE, store);
        if (block != null) return new Vector3d(block.x + 0.5, block.y + 0.5, block.z + 0.5);
        return new Vector3d(casterPos).add(0, 1.6, 0).add(SpellFx.lookDirection(caster, store).mul(AIM_RANGE));
    }

    /** Visee assistee : l'ennemi le plus proche de l'axe du regard, dans un cone de HOMING_CONE. */
    private static Ref<EntityStore> homingTarget(Ref<EntityStore> caster, Vector3d casterPos, Store<EntityStore> store) {
        Vector3d eye = new Vector3d(casterPos).add(0, 1.6, 0);
        Vector3d look = SpellFx.lookDirection(caster, store);
        Ref<EntityStore> best = null;
        double bestAngle = HOMING_CONE;
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, eye, AIM_RANGE, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            Vector3d to = center(e, store).sub(eye);
            if (to.lengthSquared() < 1e-4) continue;
            double angle = to.angle(look);
            if (angle < bestAngle) { bestAngle = angle; best = e; }
        }
        return best;
    }

    /**
     * Retourne true quand l'arme en vol est terminee (touchee, plantee ou expiree).
     * Le contact est teste sur tout le segment parcouru (position precedente -> position du tick suivant) contre la
     * boite englobante des ennemis : a 55 blocs/s l'arme avance de ~2 blocs par tick et traverserait sinon sa cible.
     */
    private static boolean tickFlying(Ref<EntityStore> caster, Volley v, Flying f, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = f.ref.isValid() ? SpellFx.position(f.ref, store) : null;
        if (pos == null && !f.spawned) {
            // Pas encore creee (CommandBuffer) : on attend ; abandon si elle n'apparait jamais
            if (now - f.launchedAt <= 1000) return false;
            if (f.ref.isValid()) cb.tryRemoveEntity(f.ref, RemoveReason.REMOVE);
            return true;
        }
        f.spawned = true;
        if (pos == null) {
            // Retiree par le moteur (contact avec une entite ou un bloc) : on teste la fin de sa trajectoire
            Vector3d end = f.lastVel != null ? new Vector3d(f.lastVel).mul(LOOK_AHEAD_S * 2).add(f.lastPos) : f.lastPos;
            Ref<EntityStore> hit = firstHit(caster, f.lastPos, end, store);
            if (hit != null) strike(caster, f, hit, f.lastPos, store, cb);
            else impact(caster, f, f.lastPos, null, store, cb);
            return true;
        }
        steer(f, pos, store);
        Velocity vel = store.getComponent(f.ref, Velocity.getComponentType());
        Vector3d velocity = vel != null ? new Vector3d(vel.getVelocity()) : f.lastVel;
        Vector3d from = f.lastPos != null ? f.lastPos : pos;
        Vector3d to = velocity != null ? new Vector3d(velocity).mul(LOOK_AHEAD_S).add(pos) : pos;
        Ref<EntityStore> hit = firstHit(caster, from, to, store);
        if (hit != null) {
            strike(caster, f, hit, pos, store, cb);
            plant(v, f, pos, hit, now, store);
            return true;
        }
        boolean stuck = f.lastPos != null && now - f.launchedAt > 150 && pos.distanceSquared(f.lastPos) < 0.0025;
        f.lastPos = pos;
        if (velocity != null && velocity.lengthSquared() > 1) f.lastVel = velocity;
        if (stuck) {                                          // plantee dans le sol / un mur
            impact(caster, f, pos, null, store, cb);
            plant(v, f, pos, null, now, store);
            return true;
        }
        if (now - f.launchedAt > FLIGHT_TIMEOUT_MS) {
            SpellFx.fx(FX_HIT, pos, 0.6f, store, cb);
            cb.tryRemoveEntity(f.ref, RemoveReason.REMOVE);
            return true;
        }
        return false;
    }

    /** Degats + knockback sur l'ennemi touche, puis eclat d'impact. */
    private static void strike(Ref<EntityStore> caster, Flying f, Ref<EntityStore> target, Vector3d pos,
                               Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d push = f.lastVel != null ? new Vector3d(f.lastVel.x, 0, f.lastVel.z) : new Vector3d();
        if (push.lengthSquared() > 1e-4) push.normalize().mul(6).add(0, 3, 0); else push = null;
        SpellFx.damage(caster, target, "Physical", WEAPON_DAMAGE, push, "Impact_Light", cb);
        impact(caster, f, pos, target, store, cb);
    }

    /** Premier ennemi dont la boite (elargie de HIT_MARGIN) coupe le segment a -> b. */
    private static Ref<EntityStore> firstHit(Ref<EntityStore> caster, Vector3d a, Vector3d b, Store<EntityStore> store) {
        Vector3d mid = new Vector3d(a).add(b).mul(0.5);
        double reach = a.distance(b) / 2 + 4.0;
        Ref<EntityStore> best = null;
        double bestT = Double.MAX_VALUE;
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, mid, reach, store)) {
            double t = segmentHitsBox(a, b, box(e, store));
            if (t >= 0 && t < bestT) { bestT = t; best = e; }
        }
        return best;
    }

    /** Boite englobante monde de l'entite, elargie de HIT_MARGIN (repli : 0.6 x 2 blocs). */
    private static double[] box(Ref<EntityStore> e, Store<EntityStore> store) {
        Vector3d p = SpellFx.position(e, store);
        if (p == null) return null;
        BoundingBox bb = store.getComponent(e, BoundingBox.getComponentType());
        Box b = bb != null ? bb.getBoundingBox() : null;
        double m = HIT_MARGIN;
        if (b == null) return new double[]{p.x - 0.3 - m, p.y - m, p.z - 0.3 - m, p.x + 0.3 + m, p.y + 2 + m, p.z + 0.3 + m};
        return new double[]{p.x + b.min.x - m, p.y + b.min.y - m, p.z + b.min.z - m, p.x + b.max.x + m, p.y + b.max.y + m, p.z + b.max.z + m};
    }

    /** Intersection segment / boite (methode des dalles) : t dans [0, 1] du premier contact, -1 sinon. */
    private static double segmentHitsBox(Vector3d a, Vector3d b, double[] box) {
        if (box == null) return -1;
        double t0 = 0, t1 = 1;
        double[] o = {a.x, a.y, a.z}, d = {b.x - a.x, b.y - a.y, b.z - a.z};
        for (int i = 0; i < 3; i++) {
            if (Math.abs(d[i]) < 1e-9) {
                if (o[i] < box[i] || o[i] > box[i + 3]) return -1;
                continue;
            }
            double ta = (box[i] - o[i]) / d[i], tb = (box[i + 3] - o[i]) / d[i];
            t0 = Math.max(t0, Math.min(ta, tb));
            t1 = Math.min(t1, Math.max(ta, tb));
            if (t0 > t1) return -1;
        }
        return t0;
    }

    /** Centre de la boite englobante (repli : 1 bloc au-dessus des pieds). */
    private static Vector3d center(Ref<EntityStore> e, Store<EntityStore> store) {
        double[] b = box(e, store);
        if (b == null) return new Vector3d();
        return new Vector3d((b[0] + b[3]) / 2, (b[1] + b[4]) / 2, (b[2] + b[5]) / 2);
    }

    /**
     * Visee assistee : oriente la trajectoire vers la cible verrouillee ; si elle n'existe plus (ou s'il n'y en avait
     * pas), l'arme se choisit en vol l'ennemi le plus aligne devant elle.
     */
    private static void steer(Flying f, Vector3d pos, Store<EntityStore> store) {
        Velocity vel = store.getComponent(f.ref, Velocity.getComponentType());
        if (vel == null) return;
        Vector3d current = new Vector3d(vel.getVelocity());
        double speed = current.length();
        if (speed < 1) return;
        if (f.target == null || !f.target.isValid()) f.target = acquire(f, pos, current, store);
        if (f.target == null) return;
        Vector3d desired = center(f.target, store).sub(pos);
        if (desired.lengthSquared() < 1e-4) return;
        desired.normalize().mul(speed);
        Vector3d next = current.lerp(desired, HOMING_STRENGTH).normalize().mul(speed);
        vel.set(next);
        TransformComponent t = store.getComponent(f.ref, TransformComponent.getComponentType());
        if (t != null) t.setRotation(pointing(next));
    }

    private static Ref<EntityStore> acquire(Flying f, Vector3d pos, Vector3d heading, Store<EntityStore> store) {
        Ref<EntityStore> best = null;
        double bestAngle = HOMING_REACQUIRE_CONE;
        for (Ref<EntityStore> e : TargetUtil.getAllEntitiesInSphere(pos, HOMING_REACQUIRE_RANGE, store)) {
            if (f.owner == null || !SpellFx.isEnemy(f.owner, e, store)) continue;
            Vector3d to = center(e, store).sub(pos);
            if (to.lengthSquared() < 1e-4) continue;
            double angle = to.angle(heading);
            if (angle < bestAngle) { bestAngle = angle; best = e; }
        }
        return best;
    }

    /** Eclat a l'impact ; les armes de la salve explosent en lumiere (degats de zone). */
    private static void impact(Ref<EntityStore> caster, Flying f, Vector3d pos, Ref<EntityStore> directHit,
                               Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx(FX_HIT, pos, f.salvo ? 2.6f : 1.0f, store, cb);
        SpellFx.sound(SFX_HIT, pos, cb);
        if (!f.salvo) return;
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, SALVO_BLAST_RADIUS, store)) {
            if (!e.equals(directHit)) SpellFx.damage(caster, e, "Physical", SALVO_BLAST_DAMAGE, null, "Impact_Light", cb);
        }
    }

    /** L'arme reste plantee (dans la cible, qu'elle suit, ou sur place) avant de se dissoudre. */
    private static void plant(Volley v, Flying f, Vector3d pos, Ref<EntityStore> target, long now, Store<EntityStore> store) {
        if (!f.ref.isValid()) return;
        TransformComponent t = store.getComponent(f.ref, TransformComponent.getComponentType());
        Rotation3f rot = t != null ? new Rotation3f(t.getRotation()) : null;
        Vector3d targetPos = target != null ? SpellFx.position(target, store) : null;
        Vector3d offset = targetPos != null ? new Vector3d(pos).sub(targetPos) : new Vector3d(pos);
        v.planted.add(new Planted(f.ref, targetPos != null ? target : null, offset, rot, now + PLANTED_MS));
    }

    /** Retourne true quand l'arme plantee s'est dissoute. */
    private static boolean tickPlanted(Planted p, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (!p.ref.isValid()) return true;
        Vector3d at = new Vector3d(p.offset);
        if (p.target != null) {
            Vector3d tp = p.target.isValid() ? SpellFx.position(p.target, store) : null;
            if (tp == null) { dissolve(p, store, cb); return true; }
            at.add(tp);
        }
        if (now >= p.until) { dissolve(p, store, cb); return true; }
        place(p.ref, at, p.rotation, store);
        return false;
    }

    private static void dissolve(Planted p, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(p.ref, store);
        if (pos != null) SpellFx.fx(FX_AURA, pos, 1.2f, store, cb);  // poussiere doree
        cb.tryRemoveEntity(p.ref, RemoveReason.REMOVE);
    }

    private static Ref<EntityStore> spawn(Ref<EntityStore> caster, String configId, Vector3d pos, Vector3d dir, CommandBuffer<EntityStore> cb) {
        ProjectileConfig config = ProjectileConfig.getAssetMap().getAsset(configId);
        if (config == null) {
            EldaniorLogger.error("GilgameshBabylon: projectile introuvable " + configId);
            return null;
        }
        try {
            return ProjectileModule.get().spawnProjectile(caster, cb, config, new Vector3d(pos), new Vector3d(dir));
        } catch (Exception e) {
            EldaniorLogger.error("GilgameshBabylon: apparition " + configId, e);
            return null;
        }
    }

    public static void handleDisconnect(UUID uuid) {
        volleys.remove(uuid);
        pendingLaunches.remove(uuid);
        seenAttacks.remove(uuid);
    }
}
