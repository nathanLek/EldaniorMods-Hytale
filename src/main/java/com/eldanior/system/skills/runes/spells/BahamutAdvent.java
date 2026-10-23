package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Avenement de Bahamut — sort divin ultime de la classe Dragon (Rune_AvenementBahamut).
 *
 * Chronologie :
 *  - rugissement : tremblement, les ennemis proches sont terrifies ({@link #TERROR_EFFECT} : ralentis, incapables
 *    d'attaquer ; les creatures sont figees) ; des ailes de dragon poussent dans le dos du lanceur ;
 *  - Bahamut, dragon spectral dore (dragon de feu vanilla x4), surgit derriere le lanceur et s'eleve au-dessus de la zone ;
 *  - trois passages en pique, chacun vise un ennemi : en rase-motte il crache le feu (degats + brulure une fois par
 *    passage et par cible) et laisse une trainee de flammes au sol qui brule pendant {@link #TRAIL_MS} ;
 *  - il se pose au centre, charge et declenche le Megaflare (explosion colossale qui projette tout), puis se dissipe.
 * Le dragon est un modele porte par une entite projectile immobile, deplacee a chaque tick le long d'une trajectoire
 * a points de passage. Le lanceur, son groupe, sa guilde et sa famille sont epargnes.
 */
public final class BahamutAdvent {

    private BahamutAdvent() {}

    // ===== Reglages =====
    private static final double TARGET_RANGE = 40.0, FALLBACK_DISTANCE = 15.0;
    private static final double TERROR_RADIUS = 25.0;
    private static final long TERROR_MS = 3_000;
    private static final double ZONE_RADIUS = 30.0;          // ennemis vises par les passages
    private static final double PASS_RADIUS = 30.0;          // les passages entrent / sortent a 30 blocs du centre
    private static final double HIGH = 20.0, LOW = 6.0;      // altitude haute / rase-motte
    private static final long RISE_MS = 2_500;               // apparition puis montee au-dessus de la zone
    private static final long PASS_MS = 3_000;               // un passage en pique
    private static final int PASSES = 3;
    private static final long LAND_MS = 2_000, CHARGE_MS = 1_500, VANISH_MS = 600;
    private static final float BREATH_DAMAGE = 600f;
    private static final double BREATH_RADIUS = 4.5;
    private static final long BREATH_FX_EVERY_MS = 100;
    private static final double TRAIL_SPACING = 2.5;
    private static final long TRAIL_MS = 6_000;
    private static final double TRAIL_RADIUS = 2.5;
    private static final float TRAIL_DAMAGE = 40f;           // par seconde
    private static final float MEGAFLARE_DAMAGE = 2500f;
    private static final double MEGAFLARE_RADIUS = 16.0, MEGAFLARE_PUSH = 30.0, MEGAFLARE_LIFT = 12.0;
    private static final double WINGS_HEIGHT = 1.35, WINGS_BACK = 0.35;
    private static final double DRAGON_HOVER = 5.0;          // altitude du dragon pose au centre (le modele est geant)

    // ===== Assets =====
    private static final String DRAGON_PROJECTILE = "Projectile_Config_Bahamut_Dragon";
    private static final String WINGS_PROJECTILE = "Projectile_Config_Bahamut_Wings";
    static final String TERROR_EFFECT = "Bahamut_Terreur";
    private static final String BURN_EFFECT = "Muspelheim_Brulure";
    private static final String TRAIL_BURN_EFFECT = "Muspelheim_Embrasement";
    private static final String FX_BREATH = "Dragon_Breath";
    private static final String FX_GROUND_FIRE = "Impact_Fire";
    private static final String FX_TRAIL = "Bahamut_Fire_Trail";
    private static final String FX_EXPLOSION = "Explosion_Big";
    private static final String FX_PILLAR = "Muspelheim_Fire_Pillar";
    private static final String FX_GOLD = "Gilgamesh_Sword_Burst";
    private static final String FX_SMOKE = "Divine_Smoke_Short";
    private static final String SFX_ROAR = "SFX_Dragon_Void_Finale_Roar";
    private static final String SFX_BREATH = "SFX_Gobliterator_Furnace_Burst";
    private static final String SFX_MEGAFLARE = "SFX_Gobliterator_Self_Destruct_Explosion";
    private static final String CAMERA_SHAKE = "Impact_Strong";

    /** Point de passage de la trajectoire du dragon. */
    record Key(long at, Vector3d pos, boolean breath) {}

    record Trail(Vector3d pos, long until) {}

    static final class Advent {
        final Vector3d center;
        final long startAt;
        final List<Key> path = new ArrayList<>();
        final List<Trail> trails = new ArrayList<>();
        final Set<Ref<EntityStore>> breathHit = new HashSet<>();
        Ref<EntityStore> dragon, wings;
        int pass = -1;
        long lastBreathFx, lastTrailPulse, megaflareAt, endAt;
        Vector3d lastTrailPos;
        boolean opened, charged, exploded;
        Advent(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; }
    }

    private static final Map<UUID, List<Advent>> advents = new ConcurrentHashMap<>();

    /** Rugissement immediat, puis le dragon prend le relais (tick). Appele depuis l'interaction (thread du monde). */
    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        Vector3d casterPos = SpellFx.position(caster, store);
        if (uuid == null || casterPos == null) return;
        Vector3d center = SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK_DISTANCE);
        Advent a = new Advent(center, System.currentTimeMillis());
        planPath(caster, a, casterPos, store);
        advents.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(a);

        // Rugissement : terreur sur les ennemis proches
        SpellFx.sound(SFX_ROAR, casterPos, store);
        SpellFx.shake(casterPos, TERROR_RADIUS * 1.6, CAMERA_SHAKE, store);
        SpellFx.fx(FX_GOLD, new Vector3d(casterPos).add(0, 1.5, 0), 3.0f, store, store);
        for (Ref<EntityStore> target : SpellFx.enemiesAround(caster, casterPos, TERROR_RADIUS, store)) {
            SpellFx.effect(target, TERROR_EFFECT, store);
            SpellFx.lockInPlace(target, TERROR_MS, store);
        }
    }

    /** Trajectoire : derriere le lanceur -> au-dessus de la zone -> 3 piques (chacun vise un ennemi) -> atterrissage. */
    private static void planPath(Ref<EntityStore> caster, Advent a, Vector3d casterPos, Store<EntityStore> store) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Vector3d look = SpellFx.lookDirection(caster, store);
        Vector3d fwd = new Vector3d(look.x, 0, look.z);
        if (fwd.lengthSquared() < 1e-6) fwd.set(0, 0, 1);
        fwd.normalize();
        long t = a.startAt;
        a.path.add(new Key(t, new Vector3d(casterPos).add(0, 6, 0).sub(new Vector3d(fwd).mul(8)), false));
        double angle = Math.atan2(-fwd.z, -fwd.x);                      // on entre dans la zone face au lanceur
        Vector3d entry = onCircle(a.center, angle, HIGH);
        t += RISE_MS;
        a.path.add(new Key(t, entry, false));
        List<Ref<EntityStore>> enemies = SpellFx.enemiesAround(caster, a.center, ZONE_RADIUS, store);
        for (int k = 0; k < PASSES; k++) {
            Vector3d low;
            if (!enemies.isEmpty()) {
                Vector3d p = SpellFx.position(enemies.get(rng.nextInt(enemies.size())), store);
                low = p != null ? new Vector3d(p) : randomGround(a.center, rng, store);
            } else {
                low = randomGround(a.center, rng, store);
            }
            low.y += LOW;
            angle += Math.toRadians(150 + rng.nextDouble(-20, 20));       // ressort de l'autre cote de la zone
            Vector3d exit = onCircle(a.center, angle, HIGH);
            a.path.add(new Key(t + PASS_MS / 2, low, true));
            t += PASS_MS;
            a.path.add(new Key(t, exit, false));
        }
        t += LAND_MS;
        a.path.add(new Key(t, new Vector3d(a.center).add(0, DRAGON_HOVER, 0), false));
        a.megaflareAt = t + CHARGE_MS;
        a.endAt = a.megaflareAt + VANISH_MS;
    }

    private static Vector3d onCircle(Vector3d c, double angle, double height) {
        return new Vector3d(c.x + Math.cos(angle) * PASS_RADIUS, c.y + height, c.z + Math.sin(angle) * PASS_RADIUS);
    }

    private static Vector3d randomGround(Vector3d c, ThreadLocalRandom rng, Store<EntityStore> store) {
        double r = 15 * Math.sqrt(rng.nextDouble()), a = rng.nextDouble(Math.PI * 2);
        return SpellFx.ground(SpellFx.world(store), new Vector3d(c.x + Math.cos(a) * r, c.y, c.z + Math.sin(a) * r));
    }

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Advent> list = advents.get(uuid);
        if (list == null) return;
        long now = System.currentTimeMillis();
        for (Advent a : list) {
            if (tickAdvent(caster, a, now, store, cb)) list.remove(a);
        }
        if (list.isEmpty()) advents.remove(uuid);
    }

    /** Retourne true quand le sort est termine (dragon dissipe et trainees de feu eteintes). */
    private static boolean tickAdvent(Ref<EntityStore> caster, Advent a, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (!a.opened) {
            a.opened = true;
            a.dragon = spawn(caster, DRAGON_PROJECTILE, a.path.get(0).pos(), new Vector3d(1, 0, 0), cb);
            SpellFx.fx(FX_GOLD, a.path.get(0).pos(), 6.0f, store, cb);
            SpellFx.fx(FX_SMOKE, a.path.get(0).pos(), 6.0f, store, cb);
        }
        boolean flying = now < a.endAt;
        if (flying) {
            followWings(caster, a, store, cb);
            flyDragon(caster, a, now, store, cb);
        }
        if (!a.charged && now >= a.megaflareAt - CHARGE_MS && now < a.megaflareAt && now >= a.path.get(a.path.size() - 1).at()) {
            a.charged = true;                                            // charge du Megaflare
            SpellFx.fx(FX_GOLD, new Vector3d(a.center).add(0, 2, 0), 5.0f, store, cb);
            SpellFx.fx(FX_PILLAR, a.center, 1.5f, store, cb);
            SpellFx.sound(SFX_ROAR, a.center, cb);
        }
        if (!a.exploded && now >= a.megaflareAt) {
            a.exploded = true;
            megaflare(caster, a, store, cb);
        }
        if (!flying) {
            removeModels(a, store, cb);
        }
        tickTrails(caster, a, now, store, cb);
        return !flying && a.trails.isEmpty();
    }

    // ===================== VOL DU DRAGON =====================

    private static void flyDragon(Ref<EntityStore> caster, Advent a, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Key last = a.path.get(a.path.size() - 1);
        Key from = last, to = last;                                      // apres le dernier point : pose au centre
        int seg = a.path.size() - 1;
        for (int i = 0; i < a.path.size() - 1; i++) {
            if (now < a.path.get(i + 1).at()) { from = a.path.get(i); to = a.path.get(i + 1); seg = i; break; }
        }
        double t = to.at() == from.at() ? 1.0 : Math.min(1.0, Math.max(0.0, (now - from.at()) / (double) (to.at() - from.at())));
        double s = t * t * (3 - 2 * t);                                  // acceleration / freinage
        Vector3d pos = new Vector3d(from.pos()).lerp(to.pos(), s);
        Vector3d dir = new Vector3d(to.pos()).sub(from.pos());
        if (dir.lengthSquared() < 1e-4) dir.set(a.center).sub(pos).mul(1, 0, 1);
        if (dir.lengthSquared() < 1e-4) dir.set(1, 0, 0);
        dir.normalize();
        Rotation3f rot = new Rotation3f();
        rot.setYaw(PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(dir.x, dir.z)));
        rot.setPitch(PhysicsMath.pitchFromDirection(dir.x, dir.y * 0.5, dir.z));   // tangage adouci
        hold(a.dragon, pos, rot, store);

        // Passage en pique : souffle de feu pendant la portion basse (autour du point bas)
        boolean breathSegment = to.breath() || from.breath();
        int passIndex = (seg - 1) / 2;
        if (breathSegment && passIndex != a.pass) { a.pass = passIndex; a.breathHit.clear(); SpellFx.sound(SFX_ROAR, pos, cb); }
        boolean breathing = breathSegment && (to.breath() ? t > 0.45 : t < 0.55);
        if (breathing) breathe(caster, a, pos, dir, now, store, cb);
    }

    private static void breathe(Ref<EntityStore> caster, Advent a, Vector3d dragonPos, Vector3d dir, long now,
                                Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d ground = SpellFx.ground(SpellFx.world(store), new Vector3d(dragonPos.x, a.center.y, dragonPos.z));
        if (now - a.lastBreathFx >= BREATH_FX_EVERY_MS) {
            a.lastBreathFx = now;
            float yaw = (float) Math.toDegrees(Math.atan2(dir.x, dir.z));
            SpellFx.fx(FX_BREATH, new Vector3d(dragonPos).add(0, -1, 0), yaw, -70f, 3.0f, store, cb);
            SpellFx.fx(FX_GROUND_FIRE, new Vector3d(ground).add(0, 0.5, 0), 2.5f, store, cb);
            SpellFx.sound(SFX_BREATH, ground, cb);
        }
        if (a.lastTrailPos == null || a.lastTrailPos.distance(ground) >= TRAIL_SPACING) {
            a.lastTrailPos = new Vector3d(ground);
            a.trails.add(new Trail(new Vector3d(ground), now + TRAIL_MS));
            SpellFx.fx(FX_TRAIL, new Vector3d(ground).add(0, 0.1, 0), 0.8f, store, cb);
        }
        for (Ref<EntityStore> target : SpellFx.enemiesAround(caster, ground, BREATH_RADIUS + 2, store)) {
            if (a.breathHit.contains(target)) continue;
            Vector3d p = SpellFx.position(target, store);
            if (p == null || Math.hypot(p.x - ground.x, p.z - ground.z) > BREATH_RADIUS) continue;
            a.breathHit.add(target);
            SpellFx.damage(caster, target, "Fire", BREATH_DAMAGE, null, "Impact_Strong", cb);
            SpellFx.effect(target, BURN_EFFECT, cb);
        }
    }

    /** Trainees de feu : chaque seconde, les ennemis dessus brulent. */
    private static void tickTrails(Ref<EntityStore> caster, Advent a, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        a.trails.removeIf(t -> now >= t.until());
        if (a.trails.isEmpty() || now - a.lastTrailPulse < 1000) return;
        a.lastTrailPulse = now;
        Set<Ref<EntityStore>> burned = new HashSet<>();
        for (Trail t : a.trails) {
            for (Ref<EntityStore> target : SpellFx.enemiesAround(caster, t.pos(), TRAIL_RADIUS, store)) {
                if (!burned.add(target)) continue;
                SpellFx.damage(caster, target, "Fire", TRAIL_DAMAGE, null, null, cb);
                SpellFx.effect(target, TRAIL_BURN_EFFECT, cb);
            }
        }
    }

    /** Megaflare : explosion colossale au centre, qui projette tout. */
    private static void megaflare(Ref<EntityStore> caster, Advent a, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d c = a.center;
        SpellFx.fx(FX_EXPLOSION, new Vector3d(c).add(0, 1, 0), 12.0f, store, cb);
        SpellFx.fx(FX_GOLD, new Vector3d(c).add(0, 3, 0), 8.0f, store, cb);
        SpellFx.fx(FX_PILLAR, c, 2.5f, store, cb);
        SpellFx.fx(FX_SMOKE, new Vector3d(c).add(0, 2, 0), 12.0f, store, cb);
        for (int k = 0; k < 8; k++) {
            double ang = k * Math.PI / 4;
            Vector3d p = SpellFx.ground(SpellFx.world(store), new Vector3d(c).add(Math.cos(ang) * 9, 0, Math.sin(ang) * 9));
            SpellFx.fx(FX_EXPLOSION, new Vector3d(p).add(0, 1, 0), 5.0f, store, cb);
            SpellFx.fx(FX_TRAIL, new Vector3d(p).add(0, 0.1, 0), 1.2f, store, cb);
        }
        SpellFx.sound(SFX_MEGAFLARE, c, cb);
        SpellFx.sound(SFX_ROAR, c, cb);
        SpellFx.shake(c, MEGAFLARE_RADIUS * 4, CAMERA_SHAKE, store);
        for (Ref<EntityStore> target : SpellFx.enemiesAround(caster, c, MEGAFLARE_RADIUS, store)) {
            Vector3d p = SpellFx.position(target, store);
            if (p == null) continue;
            double ratio = 1 - 0.5 * Math.min(1.0, p.distance(c) / MEGAFLARE_RADIUS);
            Vector3d push = new Vector3d(p.x - c.x, 0, p.z - c.z);
            if (push.lengthSquared() < 1e-4) push.set(1, 0, 0);
            push.normalize().mul(MEGAFLARE_PUSH * ratio).add(0, MEGAFLARE_LIFT * ratio, 0);
            SpellFx.damage(caster, target, "Fire", (float) (MEGAFLARE_DAMAGE * ratio), push, "Impact_Strong", cb);
            SpellFx.effect(target, BURN_EFFECT, cb);
        }
    }

    // ===================== MODELES =====================

    private static void followWings(Ref<EntityStore> caster, Advent a, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return;
        Vector3d look = SpellFx.lookDirection(caster, store);
        Vector3d fwd = new Vector3d(look.x, 0, look.z);
        if (fwd.lengthSquared() < 1e-6) fwd.set(0, 0, 1);
        fwd.normalize();
        Vector3d at = new Vector3d(pos).add(0, WINGS_HEIGHT, 0).sub(new Vector3d(fwd).mul(WINGS_BACK));
        if (a.wings == null) a.wings = spawn(caster, WINGS_PROJECTILE, at, fwd, cb);
        Rotation3f rot = new Rotation3f();
        rot.setYaw(PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(fwd.x, fwd.z)));
        hold(a.wings, at, rot, store);
    }

    private static void removeModels(Advent a, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (a.dragon != null && a.dragon.isValid()) {
            Vector3d p = SpellFx.position(a.dragon, store);
            if (p != null) { SpellFx.fx(FX_GOLD, p, 6.0f, store, cb); SpellFx.fx(FX_SMOKE, p, 6.0f, store, cb); }
            cb.tryRemoveEntity(a.dragon, RemoveReason.REMOVE);
        }
        if (a.wings != null && a.wings.isValid()) cb.tryRemoveEntity(a.wings, RemoveReason.REMOVE);
        a.dragon = null;
        a.wings = null;
    }

    private static void hold(Ref<EntityStore> ref, Vector3d pos, Rotation3f rot, Store<EntityStore> store) {
        if (ref == null || !ref.isValid()) return;
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (t == null) return;
        t.setPosition(pos);
        if (rot != null) t.setRotation(rot);
        Velocity v = store.getComponent(ref, Velocity.getComponentType());
        if (v != null) v.setZero();
    }

    private static Ref<EntityStore> spawn(Ref<EntityStore> caster, String configId, Vector3d pos, Vector3d dir, CommandBuffer<EntityStore> cb) {
        ProjectileConfig config = ProjectileConfig.getAssetMap().getAsset(configId);
        if (config == null) {
            EldaniorLogger.error("BahamutAdvent: projectile introuvable " + configId);
            return null;
        }
        try {
            return ProjectileModule.get().spawnProjectile(caster, cb, config, new Vector3d(pos), new Vector3d(dir));
        } catch (Exception e) {
            EldaniorLogger.error("BahamutAdvent: " + configId, e);
            return null;
        }
    }

    public static void handleDisconnect(UUID uuid) { advents.remove(uuid); }
}
