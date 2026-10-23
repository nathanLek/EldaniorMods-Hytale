package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sanctuaire d'Apollon — sort divin de lumiere (Rune_SanctuaireApollon), dieu grec de la lumiere, de la guerison,
 * de l'archerie et de la musique.
 *
 * Un tholos grec de marbre et d'or (16 colonnes, braseros, guirlandes de laurier, coupole a oculus ou brille le soleil,
 * autel et statue doree d'Apollon) sort du sol sur la zone visee, traverse par une colonne de lumiere, sur un cercle
 * runique dore et un sol sacre, pendant {@link #DURATION_MS}.
 *  - Allies a l'interieur (lanceur, groupe, guilde, famille), chaque seconde : vie, mana et endurance restaurees
 *    (Choeur des Muses : plus ils sont nombreux, plus le soin est fort), purification continue (immunite aux controles),
 *    -20 % de degats subis et +15 % de degats infliges (Hymne d'Apollon), couronne de laurier au-dessus de la tete.
 *  - Grace d'Apollon : un allie qui devrait mourir dans le sanctuaire est sauve une fois (soin + 2 s d'invulnerabilite).
 *  - Ennemis : la barriere sacree les repousse hors du temple en les brulant ; le soleil de l'oculus leur tire des
 *    fleches solaires (degats + aveuglement).
 *  - Apotheose : a la fin, le temple se dissout en lumiere : grand soin et mana plein pour les allies, onde de lumiere
 *    qui foudroie les ennemis.
 */
public final class ApolloSanctuary {

    private ApolloSanctuary() {}

    // ===== Reglages =====
    private static final double TARGET_RANGE = 40.0, FALLBACK_DISTANCE = 8.0;
    static final double RADIUS = 12.0;
    private static final long DURATION_MS = 20_000;
    private static final long RISE_MS = 1_500;               // le temple sort du sol
    private static final double TEMPLE_DEPTH = 7.0;          // profondeur de depart du temple
    private static final float HEAL = 0.04f, MANA = 0.05f, STAMINA = 0.10f;   // par seconde, en % du maximum
    private static final float CHOIR_PER_ALLY = 0.10f, CHOIR_MAX = 1.5f;       // Choeur des Muses
    private static final float PROTECTION = 0.8f;            // degats subis par un allie dans le sanctuaire
    private static final float HYMN_DAMAGE = 1.15f;          // degats infliges par un allie dans le sanctuaire
    private static final float GRACE_HEAL = 0.30f;           // vie rendue par la Grace d'Apollon
    private static final long GRACE_MS = 2_000;
    private static final float SACRED_FIRE_DAMAGE = 80f;     // par seconde, ennemis dans le sanctuaire
    private static final double BARRIER_PUSH = 10.0, BARRIER_LIFT = 4.0;
    private static final long ARROW_EVERY_MS = 1_500;
    private static final double ARROW_RANGE = RADIUS + 6, ARROW_SPEED = 45.0;
    private static final float ARROW_DAMAGE = 150f;
    private static final float APOTHEOSIS_HEAL = 0.40f, APOTHEOSIS_DAMAGE = 1000f;
    private static final double APOTHEOSIS_PUSH = 18.0, APOTHEOSIS_LIFT = 8.0;
    private static final double SUN_HEIGHT = 15.9;           // dans l'oculus de la coupole (252 unites x echelle 4 / 64)
    private static final double STATUE_HEIGHT = 2.0;         // sur l'autel
    private static final double BEAM_BASE = 8.9;             // la colonne de lumiere jaillit du soleil tenu par la statue (74 unites x echelle 6 / 64 + autel)
    private static final double LAUREL_HEIGHT = 2.15;        // au-dessus de la tete
    private static final double RAIN_SPACING = 12.0;
    private static final long PULSE_EVERY_MS = 4_000;

    // ===== Assets =====
    private static final String TEMPLE_PROJECTILE = "Projectile_Config_Apollo_Temple";
    private static final String SUN_PROJECTILE = "Projectile_Config_Apollo_Sun";
    private static final String BEAM_PROJECTILE = "Projectile_Config_Apollo_Beam";
    private static final String FLOOR_PROJECTILE = "Projectile_Config_Apollo_Floor";
    private static final String STATUE_PROJECTILE = "Projectile_Config_Apollo_Statue";
    private static final String LAUREL_PROJECTILE = "Projectile_Config_Apollo_Laurel";
    private static final String ARROW_PROJECTILE = "Projectile_Config_Apollo_Arrow";
    private static final String FX_SACRED_GROUND = "Apollo_Sacred_Ground";
    private static final String FX_HEAL = "Apollo_Heal_Pulse";
    private static final String FX_ZONE = "Apollo_Zone";
    private static final String FX_RAIN = "Apollo_Light_Rain";
    private static final String FX_GOLD = "Gilgamesh_Sword_Burst";
    private static final String BLESSING_EFFECT = "Apollo_Benediction";
    private static final String SACRED_FIRE_EFFECT = "Apollo_Feu_Sacre";
    private static final String BLIND_EFFECT = "Apollo_Aveugle";
    private static final String GRACE_EFFECT = "Apollo_Grace";
    private static final String SFX_CAST = "SFX_Deployable_Totem_Heal_Spawn";
    private static final String SFX_BURN = "SFX_Effect_Burn_World";
    private static final String SFX_ARROW = "SFX_Staff_Flame_Fireball_Launch";
    private static final String SFX_FINAL = "SFX_Golem_Frost_Slam_Impact";

    record SolarArrow(Ref<EntityStore> model, Ref<EntityStore> target, long impactAt) {}

    static final class Sanctuary {
        final Vector3d center, fwd;
        final long startAt;
        final Set<Ref<EntityStore>> graced = new HashSet<>();
        final Map<Ref<EntityStore>, Ref<EntityStore>> crowns = new HashMap<>();
        final List<SolarArrow> arrows = new ArrayList<>();
        Ref<EntityStore> temple, sun, beam, floor, statue;
        long lastPulse, lastAltarPulse, lastArrow;
        boolean opened;
        Sanctuary(Vector3d center, Vector3d fwd, long startAt) { this.center = center; this.fwd = fwd; this.startAt = startAt; }
    }

    private static final Map<UUID, List<Sanctuary>> sanctuaries = new ConcurrentHashMap<>();
    /** Allies actuellement dans un sanctuaire (fin de protection) et sanctuaire qui les protege, lus par DivineDamageSystem. */
    private static final Map<Ref<EntityStore>, Long> protectedUntil = new ConcurrentHashMap<>();
    private static final Map<Ref<EntityStore>, Sanctuary> protector = new ConcurrentHashMap<>();
    /** Invulnerabilite de la Grace d'Apollon (fin). */
    private static final Map<Ref<EntityStore>, Long> graceUntil = new ConcurrentHashMap<>();

    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        if (uuid == null) return;
        Vector3d center = SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK_DISTANCE);
        sanctuaries.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>())
                .add(new Sanctuary(center, SpellFx.forward(caster, store), System.currentTimeMillis()));
    }

    // ===================== HOOKS DE DEGATS (DivineDamageSystem) =====================

    private static boolean isProtected(Ref<EntityStore> ref) {
        if (protectedUntil.isEmpty() || ref == null) return false;
        Long until = protectedUntil.get(ref);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) { protectedUntil.remove(ref); protector.remove(ref); return false; }
        return true;
    }

    /** -20 % de degats subis pour un allie dans un sanctuaire actif. */
    public static float damageTakenMultiplier(Ref<EntityStore> target) {
        return isProtected(target) ? PROTECTION : 1f;
    }

    /** Hymne d'Apollon : +15 % de degats infliges par un allie dans le sanctuaire. */
    public static float damageDealtMultiplier(Ref<EntityStore> attacker) {
        return isProtected(attacker) ? HYMN_DAMAGE : 1f;
    }

    /** Invulnerabilite de la Grace d'Apollon. */
    public static boolean isInvulnerable(Ref<EntityStore> target) {
        if (graceUntil.isEmpty() || target == null) return false;
        Long until = graceUntil.get(target);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) { graceUntil.remove(target); return false; }
        return true;
    }

    /**
     * Grace d'Apollon : si ce coup tuerait un allie protege (une seule fois par sanctuaire), il est annule, l'allie
     * est soigne et rendu invulnerable {@link #GRACE_MS}. Retourne true si le coup doit etre annule.
     */
    public static boolean tryGrace(Ref<EntityStore> target, float amount, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (!isProtected(target)) return false;
        Sanctuary s = protector.get(target);
        if (s == null || s.graced.contains(target)) return false;
        try {
            var map = store.getComponent(target, com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap.getComponentType());
            var health = map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
            if (health == null || amount < health.get()) return false;
        } catch (Exception e) {
            return false;
        }
        s.graced.add(target);
        graceUntil.put(target, System.currentTimeMillis() + GRACE_MS);
        SpellFx.restore(target, DefaultEntityStatTypes.getHealth(), GRACE_HEAL, cb);
        SpellFx.effect(target, GRACE_EFFECT, cb);
        Vector3d p = SpellFx.position(target, store);
        if (p != null) {
            SpellFx.fx(FX_GOLD, new Vector3d(p).add(0, 1, 0), 4.0f, store, cb);
            SpellFx.fx(FX_HEAL, new Vector3d(p).add(0, 1, 0), 2.0f, store, cb);
            SpellFx.sound(SFX_CAST, p, cb);
        }
        return true;
    }

    // ===================== DEROULEMENT =====================

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Sanctuary> list = sanctuaries.get(uuid);
        if (list == null) return;
        long now = System.currentTimeMillis();
        for (Sanctuary s : list) if (tickSanctuary(caster, s, now, store, cb)) list.remove(s);
        if (list.isEmpty()) sanctuaries.remove(uuid);
    }

    private static boolean tickSanctuary(Ref<EntityStore> caster, Sanctuary s, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (!s.opened) { s.opened = true; open(caster, s, store, cb); }
        long elapsed = now - s.startAt;
        if (elapsed >= DURATION_MS) {
            apotheosis(caster, s, store, cb);
            return true;
        }
        // Le temple sort du sol avec sa statue ; soleil, lumiere et rayons
        double rise = Math.min(1.0, elapsed / (double) RISE_MS);
        double sunk = TEMPLE_DEPTH * (1 - rise * rise * (3 - 2 * rise));
        SpellFx.hold(s.temple, new Vector3d(s.center).sub(0, sunk, 0), null, store);
        SpellFx.hold(s.statue, new Vector3d(s.center).add(0, STATUE_HEIGHT - sunk, 0), SpellFx.facing(new Vector3d(s.fwd).negate(), false), store);   // face au lanceur
        SpellFx.hold(s.sun, new Vector3d(s.center).add(0, SUN_HEIGHT - sunk + Math.sin(now / 600.0) * 0.3, 0), null, store);
        SpellFx.hold(s.beam, new Vector3d(s.center).add(0, BEAM_BASE - sunk, 0), null, store);
        SpellFx.hold(s.floor, new Vector3d(s.center).add(0, 0.12, 0), null, store);
        followCrowns(s, store, cb);
        tickArrows(caster, s, now, store, cb);
        // Pulsation de l'autel : eclat dore et onde de lumiere
        if (rise >= 1 && now - s.lastAltarPulse >= PULSE_EVERY_MS) {
            s.lastAltarPulse = now;
            SpellFx.fx(FX_GOLD, new Vector3d(s.center).add(0, 2.5, 0), 4.0f, store, cb);
            SpellFx.fx(FX_GOLD, new Vector3d(s.center).add(0, SUN_HEIGHT, 0), 5.0f, store, cb);
            SpellFx.sound(SFX_CAST, s.center, cb);
        }
        // Fleches solaires tirees par le soleil de l'oculus
        if (rise >= 1 && now - s.lastArrow >= ARROW_EVERY_MS) {
            s.lastArrow = now;
            shootArrow(caster, s, now, store, cb);
        }
        if (now - s.lastPulse < 1000) return false;
        s.lastPulse = now;

        // Allies : soin (Choeur des Muses), mana, endurance, purification continue, protection, Hymne, laurier
        List<Ref<EntityStore>> inside = new ArrayList<>();
        for (Ref<EntityStore> ally : SpellFx.alliesAround(caster, s.center, RADIUS + 2, store)) {
            Vector3d p = SpellFx.position(ally, store);
            if (p != null && SpellFx.horizontalDistance(p, s.center) <= RADIUS) inside.add(ally);
        }
        float choir = Math.min(CHOIR_MAX, 1f + CHOIR_PER_ALLY * Math.max(0, inside.size() - 1));
        for (Ref<EntityStore> ally : inside) {
            Vector3d p = SpellFx.position(ally, store);
            SpellFx.restore(ally, DefaultEntityStatTypes.getHealth(), HEAL * choir, cb);
            SpellFx.restore(ally, DefaultEntityStatTypes.getMana(), MANA, cb);
            SpellFx.restore(ally, DefaultEntityStatTypes.getStamina(), STAMINA, cb);
            EffectsManager.cureAllDebuffs(ally, cb);                   // immunite aux controles (gel, paralysie...)
            SpellFx.effect(ally, BLESSING_EFFECT, cb);
            if (p != null && (now / 1000) % 3 == 0) SpellFx.fx(FX_HEAL, new Vector3d(p).add(0, 1, 0), 0.6f, store, cb);   // gerbe de soin (toutes les 3 s)
            protectedUntil.put(ally, now + 1_500);
            protector.put(ally, s);
            if (!s.crowns.containsKey(ally) && p != null) {
                s.crowns.put(ally, SpellFx.spawnModel(caster, LAUREL_PROJECTILE, new Vector3d(p).add(0, LAUREL_HEIGHT, 0), new Vector3d(1, 0, 0), cb));
            }
        }
        // Ennemis : barriere sacree (repousses hors du temple) et feu sacre
        for (Ref<EntityStore> enemy : SpellFx.enemiesAround(caster, s.center, RADIUS + 2, store)) {
            Vector3d p = SpellFx.position(enemy, store);
            if (p == null || SpellFx.horizontalDistance(p, s.center) > RADIUS) continue;
            Vector3d out = new Vector3d(p.x - s.center.x, 0, p.z - s.center.z);
            if (out.lengthSquared() < 1e-4) out.set(s.fwd);
            out.normalize().mul(BARRIER_PUSH).add(0, BARRIER_LIFT, 0);
            SpellFx.damage(caster, enemy, "Fire", SACRED_FIRE_DAMAGE, out, null, cb);
            SpellFx.effect(enemy, SACRED_FIRE_EFFECT, cb);
            SpellFx.fx(FX_GOLD, new Vector3d(p).add(0, 1, 0), 0.8f, store, cb);
            SpellFx.sound(SFX_BURN, p, cb);
        }
        return false;
    }

    /** Couronnes de laurier : suivent la tete des allies benis, disparaissent quand ils quittent le sanctuaire. */
    private static void followCrowns(Sanctuary s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        s.crowns.entrySet().removeIf(e -> {
            Ref<EntityStore> ally = e.getKey();
            Vector3d p = ally.isValid() ? SpellFx.position(ally, store) : null;
            if (p == null || !isProtected(ally)) { SpellFx.remove(e.getValue(), cb); return true; }
            SpellFx.hold(e.getValue(), new Vector3d(p).add(0, LAUREL_HEIGHT, 0), null, store);
            return false;
        });
    }

    private static void shootArrow(Ref<EntityStore> caster, Sanctuary s, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d from = new Vector3d(s.center).add(0, SUN_HEIGHT, 0);
        Ref<EntityStore> best = null;
        double bestD = Double.MAX_VALUE;
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, s.center, ARROW_RANGE + 4, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || SpellFx.horizontalDistance(p, s.center) > ARROW_RANGE) continue;
            double d = SpellFx.horizontalDistance(p, s.center);
            if (d < bestD) { bestD = d; best = e; }
        }
        if (best == null) return;
        Vector3d to = SpellFx.position(best, store).add(0, 1, 0);
        Vector3d dir = new Vector3d(to).sub(from);
        double dist = dir.length();
        if (dist < 1e-3) return;
        dir.normalize();
        Ref<EntityStore> arrow = SpellFx.spawnModel(caster, ARROW_PROJECTILE, from, dir, cb);
        s.arrows.add(new SolarArrow(arrow, best, now + (long) (dist / ARROW_SPEED * 1000)));
        SpellFx.fx(FX_GOLD, from, 1.5f, store, cb);
        SpellFx.sound(SFX_ARROW, from, cb);
    }

    private static void tickArrows(Ref<EntityStore> caster, Sanctuary s, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        s.arrows.removeIf(a -> {
            if (now < a.impactAt()) return false;
            Vector3d p = a.target().isValid() ? SpellFx.position(a.target(), store) : null;
            if (p != null) {
                SpellFx.damage(caster, a.target(), "Fire", ARROW_DAMAGE, null, "Impact_Light", cb);
                SpellFx.effect(a.target(), BLIND_EFFECT, cb);
                SpellFx.fx(FX_GOLD, new Vector3d(p).add(0, 1, 0), 1.6f, store, cb);
            }
            SpellFx.remove(a.model(), cb);
            return true;
        });
    }

    /** Apotheose : le temple se dissout en lumiere, grand soin pour les allies, onde de lumiere sur les ennemis. */
    private static void apotheosis(Ref<EntityStore> caster, Sanctuary s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        for (int i = 0; i < 16; i++) {
            double a = 2 * Math.PI * i / 16;
            Vector3d col = new Vector3d(s.center).add(Math.cos(a) * RADIUS, 4, Math.sin(a) * RADIUS);
            SpellFx.fx(FX_GOLD, col, 3.0f, store, cb);
        }
        SpellFx.fx(FX_GOLD, new Vector3d(s.center).add(0, 2, 0), 10.0f, store, cb);
        SpellFx.fx(FX_GOLD, new Vector3d(s.center).add(0, SUN_HEIGHT, 0), 10.0f, store, cb);
        SpellFx.sound(SFX_FINAL, s.center, cb);
        SpellFx.sound(SFX_CAST, s.center, cb);
        SpellFx.shake(s.center, RADIUS * 3, "Impact_Strong", store);
        for (Ref<EntityStore> ally : SpellFx.alliesAround(caster, s.center, RADIUS + 2, store)) {
            Vector3d p = SpellFx.position(ally, store);
            if (p == null || SpellFx.horizontalDistance(p, s.center) > RADIUS) continue;
            SpellFx.restore(ally, DefaultEntityStatTypes.getHealth(), APOTHEOSIS_HEAL, cb);
            SpellFx.restore(ally, DefaultEntityStatTypes.getMana(), 1f, cb);
            SpellFx.restore(ally, DefaultEntityStatTypes.getStamina(), 1f, cb);
            SpellFx.fx(FX_HEAL, new Vector3d(p).add(0, 1, 0), 2.5f, store, cb);
        }
        for (Ref<EntityStore> enemy : SpellFx.enemiesAround(caster, s.center, RADIUS + 4, store)) {
            Vector3d p = SpellFx.position(enemy, store);
            if (p == null) continue;
            Vector3d out = new Vector3d(p.x - s.center.x, 0, p.z - s.center.z);
            if (out.lengthSquared() < 1e-4) out.set(s.fwd);
            out.normalize().mul(APOTHEOSIS_PUSH).add(0, APOTHEOSIS_LIFT, 0);
            SpellFx.damage(caster, enemy, "Fire", APOTHEOSIS_DAMAGE, out, "Impact_Strong", cb);
            SpellFx.effect(enemy, BLIND_EFFECT, cb);
        }
        for (Ref<EntityStore> m : new Ref[]{s.temple, s.sun, s.beam, s.floor, s.statue}) SpellFx.remove(m, cb);
        for (Ref<EntityStore> crown : s.crowns.values()) SpellFx.remove(crown, cb);
        for (SolarArrow a : s.arrows) SpellFx.remove(a.model(), cb);
        s.crowns.clear();
        s.arrows.clear();
    }

    private static void open(Ref<EntityStore> caster, Sanctuary s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d x = new Vector3d(1, 0, 0);
        s.temple = SpellFx.spawnModel(caster, TEMPLE_PROJECTILE, new Vector3d(s.center).sub(0, TEMPLE_DEPTH, 0), x, cb);
        s.statue = SpellFx.spawnModel(caster, STATUE_PROJECTILE, new Vector3d(s.center).add(0, STATUE_HEIGHT - TEMPLE_DEPTH, 0), new Vector3d(s.fwd).negate(), cb);
        s.sun = SpellFx.spawnModel(caster, SUN_PROJECTILE, new Vector3d(s.center).add(0, SUN_HEIGHT - TEMPLE_DEPTH, 0), x, cb);
        s.beam = SpellFx.spawnModel(caster, BEAM_PROJECTILE, new Vector3d(s.center).add(0, BEAM_BASE - TEMPLE_DEPTH, 0), x, cb);
        s.floor = SpellFx.spawnModel(caster, FLOOR_PROJECTILE, new Vector3d(s.center).add(0, 0.12, 0), x, cb);
        SpellFx.fx(FX_ZONE, new Vector3d(s.center).add(0, 0.05, 0), (float) (RADIUS / 3.0), store, cb);
        SpellFx.fx(FX_SACRED_GROUND, new Vector3d(s.center).add(0, 0.1, 0), (float) (RADIUS / 4.0), store, cb);
        SpellFx.fx(FX_GOLD, new Vector3d(s.center).add(0, 6, 0), 8.0f, store, cb);
        int steps = (int) Math.ceil(RADIUS / RAIN_SPACING);
        for (int i = -steps; i <= steps; i++) {
            for (int j = -steps; j <= steps; j++) {
                double dx = i * RAIN_SPACING, dz = j * RAIN_SPACING;
                if (Math.hypot(dx, dz) > RADIUS) continue;
                SpellFx.fx(FX_RAIN, new Vector3d(s.center).add(dx, 9, dz), 1.2f, store, cb);
            }
        }
        SpellFx.sound(SFX_CAST, s.center, cb);
        SpellFx.shake(s.center, RADIUS * 2, "Impact_Light", store);
    }

    public static void handleDisconnect(UUID uuid) { sanctuaries.remove(uuid); }
}
