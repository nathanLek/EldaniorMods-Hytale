package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Reveil de Gaia — sort divin de terre (Rune_ReveilGaia), Titanide mere de la Terre : plaques tectoniques.
 *
 * Le lanceur est l'epicentre ; toute la zone ({@link #RADIUS} blocs, comme la Porte de Niflheim) se souleve, sauf le
 * pilier de terre sur lequel il se tient :
 *  1. Crocs de la Terre : de grands crocs rocheux detailles (courbes, aretes, veines de lave) jaillissent un a un en
 *     bordure de zone, en laissant de larges passages ; ils se retractent dans le sol a la fin du sort.
 *  2. Fracture : le sol se fend de failles profondes remplies de lave (geysers de lave et de vapeur), qui decoupent
 *     toute la zone en ~{@link #PLATES} plaques.
 *  3. Arrachement par vagues, du centre vers l'exterieur : chaque plaque (vrais blocs : sol sur
 *     {@link #PLATE_DEPTH} blocs, plantes, arbres) s'arrache, monte tres haut en basculant, tourne autour de
 *     l'epicentre et percute les autres (eclats, debris, ennemis broyes).
 *  4. Retombee : chaque plaque se retourne et s'abat sur son trou ; chaque impact declenche un tremblement de terre
 *     (le sol ondule : les vrais blocs se soulevent et retombent, les ennemis sont projetes). Quand tout est retombe,
 *     un dernier seisme traverse toute la zone.
 * Le terrain est restaure apres {@link #RESTORE_AFTER_MS}.
 */
public final class GaiaAwakening {

    private GaiaAwakening() {}

    // ===== Zone =====
    static final double RADIUS = SpellFx.DIVINE_ZONE_RADIUS;
    private static final double CASTER_PILLAR = 2.5;                 // pilier de terre laisse sous le lanceur
    private static final int SCAN_UP = 12, SCAN_DOWN = 10;
    // Plaques
    private static final int PLATES = 16, GROUPS = 4;
    private static final double PLATE_SPACING = 10.5, CRACK_GAP = 2.0;
    private static final int PLATE_DEPTH = 6, VEGETATION_HEIGHT = 14;
    private static final int PLATE_BUDGET = 520, MAX_AIRBORNE = 2_800;   // entites-blocs par plaque / en l'air au total
    private static final int CRACK_DEPTH = 10;
    // Deroulement (ms)
    private static final long TEAR_AT = 1_500, GROUP_INTERVAL = 5_500, TEAR_STAGGER = 250;
    private static final long RISE_MS = 2_200, FLOAT_MS = 5_000, FLIP_MS = 1_300, SLAM_MS = 450;
    private static final double ALT_MIN = 18.0, ALT_MAX = 40.0, FLIP_LIFT = 6.0, TILT_MAX = 0.55, SPIN_MAX = 0.35;
    private static final double ORBIT_SPEED = 0.22;
    private static final long CLASH_EVERY_MS = 600, CLASH_MS = 900, DEBRIS_FX_EVERY_MS = 600;
    private static final int BREAK_OFF = 10;
    private static final long FALLING_LIFE_MS = 2_500;
    // Crocs : grands crocs en bordure (avec de larges passages)
    private static final int BIG_FANGS = 8, COMPANIONS = 2;
    private static final double FANG_MIN_GAP = 0.6;                   // ecart angulaire minimal entre deux grands crocs (rad)
    private static final int FANG_MIN = 14, FANG_MAX = 22, COMPANION_MIN = 6, COMPANION_MAX = 9;
    private static final double FANG_BASE_MIN = 2.8, FANG_BASE_MAX = 3.8, FANG_CURVE = 0.35;
    private static final long FANG_ERUPT_FROM = 400, FANG_ERUPT_EVERY = 700, FANG_RISE_MS = 1_200, RETRACT_MS = 1_800;
    private static final float FANG_DAMAGE = 300f;
    private static final double FANG_LAUNCH = 14.0, FANG_HIT_RADIUS = 5.0;
    // Geysers de lave
    private static final long GEYSER_EVERY_MS = 350;
    private static final int GEYSERS_PER_PULSE = 4;
    private static final double GEYSER_RADIUS = 2.5;
    private static final float GEYSER_DAMAGE = 120f;
    // Tremblements de terre (a chaque impact, et le dernier sur toute la zone)
    private static final double IMPACT_QUAKE_RADIUS = 16.0, WAVE_SPEED = 12.0, WAVE_BAND = 1.6;
    private static final long LIFT_MS = 450;
    private static final double LIFT_HEIGHT = 1.6;
    private static final int MAX_LIFTED = 320;
    private static final float WAVE_DAMAGE = 250f, CLASH_DAMAGE = 400f, TEAR_DAMAGE = 300f, SLAM_DAMAGE = 1500f, FINAL_DAMAGE = 600f;
    private static final double WAVE_LAUNCH = 9.0, WAVE_PUSH = 4.0, TEAR_LAUNCH = 14.0, CLASH_RADIUS = 7.0, SLAM_EXTRA = 2.0;
    private static final int CRATER_DEPTH = 3;
    private static final double RUBBLE_CHANCE = 0.3;
    static final long RESTORE_AFTER_MS = 180_000;
    // Blocs
    private static final String CRACK_FLOOR = "Rock_Volcanic_Cracked_Lava", LAVA = "Lava_Source";
    private static final String FANG_BODY = "Rock_Stone", FANG_BASE_ROCK = "Rock_Stone_Cobble", FANG_DIRT = "Soil_Dirt";
    private static final String FANG_RIDGE = "Rock_Shale", FANG_TIP = "Rock_Basalt", FANG_VEIN = "Rock_Volcanic_Cracked_Lava", FANG_MOSS = "Rock_Stone_Mossy";

    // ===== Assets =====
    private static final String STUN_EFFECT = "Gaia_Etourdi";
    private static final String FX_DUST = "Gaia_Dust_Burst";
    private static final String FX_EXPLOSION = "Explosion_Big";
    private static final String FX_SHOCKWAVE = "Chaos_Shockwave";
    private static final String FX_LAVA = "Block_Land_Lava_Hard", FX_STEAM = "Muspelheim_Steam_Burst";
    private static final String SFX_SLAM = "SFX_Golem_Frost_Slam_Impact";
    private static final String SFX_QUAKE = "SFX_Gobliterator_Rocket_Barrage_Explosion";

    record Piece(Ref<EntityStore> ref, Vector3d local) {}
    record Falling(Ref<EntityStore> ref, long until) {}
    record Lifted(Ref<EntityStore> ref, int x, int y, int z, String block, int rotation, double height, long startAt) {}
    record Clash(Plate a, Plate b, long startAt, double close) {}
    record Cell(int x, int y, int z, String block) {}

    /** Croc rocheux : cellules (du bas vers le haut), jaillit a eruptAt ; les crocs sous les ennemis se retractent vite. */
    static final class Fang {
        final List<Cell> cells;
        final Vector3d base;
        final long eruptAt, retractAt;
        final boolean big;
        int placed;
        boolean erupted, retracted;
        Fang(List<Cell> cells, Vector3d base, long eruptAt, long retractAt, boolean big) {
            this.cells = cells; this.base = base; this.eruptAt = eruptAt; this.retractAt = retractAt; this.big = big;
        }
    }

    static final class Plate {
        final Vector3d pivot, hub;
        final List<long[]> columns = new ArrayList<>();        // x, z, bord (1/0)
        final List<Piece> pieces = new ArrayList<>();
        final List<String> ground = new ArrayList<>();
        final double alt, tiltPitch, tiltRoll, spin, phase, orbitDir;
        final Vector3d drift = new Vector3d();
        long tearAt, slamAt;
        double yaw, pitch, roll, lift, orbit;
        boolean torn, slammed;
        Plate(Vector3d pivot, Vector3d hub, ThreadLocalRandom rng) {
            this.pivot = pivot; this.hub = hub;
            this.alt = rng.nextDouble(ALT_MIN, ALT_MAX);
            this.tiltPitch = rng.nextDouble(-TILT_MAX, TILT_MAX); this.tiltRoll = rng.nextDouble(-TILT_MAX, TILT_MAX);
            this.spin = rng.nextDouble(-SPIN_MAX, SPIN_MAX); this.phase = rng.nextDouble(Math.PI * 2);
            this.orbitDir = rng.nextBoolean() ? 1 : -1;
        }
        boolean airborne() { return torn && !slammed; }
        Vector3d center() {
            double rx = pivot.x - hub.x + drift.x, rz = pivot.z - hub.z + drift.z, c = Math.cos(orbit), s = Math.sin(orbit);
            return new Vector3d(hub.x + rx * c - rz * s, pivot.y + lift, hub.z + rx * s + rz * c);
        }
    }

    /** Tremblement de terre : un front qui avance depuis l'origine jusqu'a maxRadius et fait onduler le sol. */
    static final class Wave {
        final Vector3d origin;
        final long startAt;
        final double maxRadius, power;
        final Set<Ref<EntityStore>> hit = new HashSet<>();
        double done;
        Wave(Vector3d origin, long startAt, double maxRadius, double power) { this.origin = origin; this.startAt = startAt; this.maxRadius = maxRadius; this.power = power; }
    }

    static final class Quake {
        final Vector3d center;
        final long startAt;
        final List<Plate> plates = new ArrayList<>();
        final List<Long> cracks = new ArrayList<>();
        final List<Fang> fangs = new ArrayList<>();
        final Set<Long> fangCells = ConcurrentHashMap.newKeySet();
        final List<Clash> clashes = new ArrayList<>();
        final Set<Clash> clashHit = new HashSet<>();
        final List<Falling> falling = new ArrayList<>();
        final List<Wave> waves = new ArrayList<>();
        final List<Lifted> lifted = new ArrayList<>();
        final Set<Long> liftedCells = new HashSet<>();
        TerrainRestore.Restore terrain, waveSafety, fangBatch;
        long lastClash, lastGeyser, lastDebrisFx, retractAt = -1;
        boolean cracked, finalQuake;
        Quake(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; }
    }

    private static final Map<UUID, List<Quake>> quakes = new ConcurrentHashMap<>();

    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        Vector3d pos = SpellFx.position(caster, store);
        if (uuid == null || pos == null) return;
        Quake q = new Quake(new Vector3d(pos), System.currentTimeMillis());
        plan(q, SpellFx.world(store));
        quakes.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(q);
        SpellFx.protectFromFall(caster, 40_000, store);
        SpellFx.shake(pos, RADIUS * 2, "Impact_Strong", store);
        SpellFx.fx(FX_DUST, new Vector3d(pos).add(0, 0.5, 0), 5.0f, store, store);
        SpellFx.sound(SFX_QUAKE, pos, store);
        SpellFx.sound(SFX_SLAM, pos, store);
    }

    // ===================== PLAN =====================

    /** Plaques couvrant toute la zone (hors pilier du lanceur), failles entre elles, couronne de crocs. */
    private static void plan(Quake q, World world) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        List<Vector3d> seeds = new ArrayList<>();
        for (int tries = 0; tries < 3_000 && seeds.size() < PLATES; tries++) {
            double a = rng.nextDouble(Math.PI * 2), d = Math.sqrt(rng.nextDouble(Math.pow(CASTER_PILLAR + 5, 2), Math.pow(RADIUS - 3, 2)));
            Vector3d c = new Vector3d(q.center).add(Math.cos(a) * d, 0, Math.sin(a) * d);
            boolean free = true;
            for (Vector3d o : seeds) if (SpellFx.horizontalDistance(o, c) < PLATE_SPACING) { free = false; break; }
            if (free) seeds.add(c);
        }
        for (Vector3d s : seeds) q.plates.add(new Plate(s, q.center, rng));
        // Vagues : du centre vers l'exterieur
        List<Plate> order = new ArrayList<>(q.plates);
        order.sort((a, b) -> Double.compare(SpellFx.horizontalDistance(a.pivot, q.center), SpellFx.horizontalDistance(b.pivot, q.center)));
        for (int i = 0; i < order.size(); i++) {
            int group = i * GROUPS / Math.max(1, order.size()), inGroup = i - (group * order.size() + GROUPS - 1) / GROUPS;
            Plate p = order.get(i);
            p.tearAt = TEAR_AT + group * GROUP_INTERVAL + Math.max(0, inGroup) * TEAR_STAGGER;
            p.slamAt = p.tearAt + RISE_MS + FLOAT_MS + FLIP_MS;
        }
        // Colonnes : chaque colonne de la zone appartient a la plaque la plus proche ; failles entre elles
        int R = (int) Math.ceil(RADIUS), cx = (int) Math.floor(q.center.x), cz = (int) Math.floor(q.center.z);
        Map<Long, Integer> owner = new HashMap<>();
        for (int dx = -R; dx <= R; dx++) {
            for (int dz = -R; dz <= R; dz++) {
                double x = cx + dx + 0.5, z = cz + dz + 0.5, fromCenter = Math.hypot(x - q.center.x, z - q.center.z);
                if (fromCenter > RADIUS - 1 || fromCenter <= CASTER_PILLAR) continue;           // le pilier du lanceur reste
                double d1 = Double.MAX_VALUE, d2 = Double.MAX_VALUE;
                int best = -1;
                for (int i = 0; i < q.plates.size(); i++) {
                    Vector3d p = q.plates.get(i).pivot;
                    double d = Math.hypot(x - p.x, z - p.z) * (1 + 0.07 * Math.sin(x * 0.6 + z * 0.45 + i));
                    if (d < d1) { d2 = d1; d1 = d; best = i; } else if (d < d2) d2 = d;
                }
                if (best < 0) continue;
                long key = colKey(cx + dx, cz + dz);
                if (d2 - d1 < CRACK_GAP || fromCenter <= CASTER_PILLAR + 1.5) { q.cracks.add(key); continue; }
                owner.put(key, best);
            }
        }
        for (Map.Entry<Long, Integer> e : owner.entrySet()) {
            int x = colX(e.getKey()), z = colZ(e.getKey());
            boolean edge = false;
            for (int[] o : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                if (!e.getValue().equals(owner.get(colKey(x + o[0], z + o[1])))) { edge = true; break; }
            }
            q.plates.get(e.getValue()).columns.add(new long[]{x, z, edge ? 1 : 0});
        }
        // Grands crocs en bordure, avec de larges passages entre eux, chacun entoure de petits crocs
        List<Double> angles = new ArrayList<>();
        for (int tries = 0; tries < 500 && angles.size() < BIG_FANGS; tries++) {
            double a = rng.nextDouble(Math.PI * 2);
            boolean free = true;
            for (double o : angles) if (Math.abs(Math.atan2(Math.sin(a - o), Math.cos(a - o))) < FANG_MIN_GAP) { free = false; break; }
            if (free) angles.add(a);
        }
        for (int k = 0; k < angles.size(); k++) {
            double a = angles.get(k), r = RADIUS + rng.nextDouble(-0.5, 4.0);
            long at = FANG_ERUPT_FROM + k * FANG_ERUPT_EVERY;
            Vector3d inward = new Vector3d(-Math.cos(a), 0, -Math.sin(a));
            addFang(q, world, new Vector3d(q.center).add(Math.cos(a) * r, 0, Math.sin(a) * r), inward, rng.nextInt(FANG_MIN, FANG_MAX + 1),
                    rng.nextDouble(FANG_BASE_MIN, FANG_BASE_MAX), at, -1, true, rng);
            for (int c = 0; c < COMPANIONS; c++) {
                double ca = a + rng.nextDouble(-0.12, 0.12), cr = r + rng.nextDouble(-4, 4);
                Vector3d lean = new Vector3d(rng.nextDouble(-1, 1), 0, rng.nextDouble(-1, 1));
                addFang(q, world, new Vector3d(q.center).add(Math.cos(ca) * cr, 0, Math.sin(ca) * cr), lean.lengthSquared() > 1e-4 ? lean.normalize() : inward,
                        rng.nextInt(COMPANION_MIN, COMPANION_MAX + 1), rng.nextDouble(1.3, 1.9), at + 250 + c * 150L, -1, false, rng);
            }
        }
    }

    private static void addFang(Quake q, World world, Vector3d base, Vector3d lean, int height, double baseRadius, long eruptAt, long retractAt, boolean big, ThreadLocalRandom rng) {
        int top = groundY(world, (int) Math.floor(base.x), (int) Math.floor(q.center.y), (int) Math.floor(base.z));
        if (top == Integer.MIN_VALUE) return;
        List<Cell> cells = detailedFang(new Vector3d(base.x, top + 1, base.z), lean, height, baseRadius, rng);
        q.fangs.add(new Fang(cells, new Vector3d(base.x, top + 1, base.z), eruptAt, retractAt, big));
    }

    /**
     * Croc rocheux detaille : section a aretes qui s'affine et se courbe vers `lean` en montant ; base de terre et de
     * pierre concassee, corps de pierre a aretes de schiste, mousse et veines de lave dans le bas, pointe de basalte.
     */
    private static List<Cell> detailedFang(Vector3d base, Vector3d lean, int height, double baseRadius, ThreadLocalRandom rng) {
        Map<Long, Cell> cells = new HashMap<>();
        int y0 = (int) Math.floor(base.y);
        double twist = rng.nextDouble(Math.PI * 2);
        for (int i = 0; i < height; i++) {
            double u = i / (double) height;
            double r = baseRadius * Math.pow(1 - u, 0.85) + 0.35;
            double bend = FANG_CURVE * height * u * u;                                     // se courbe de plus en plus
            double ox = base.x + lean.x * bend, oz = base.z + lean.z * bend;
            int R = (int) Math.ceil(r * 1.25);
            for (int dx = -R; dx <= R; dx++) for (int dz = -R; dz <= R; dz++) {
                double th = Math.atan2(dz, dx), ridge = Math.sin(5 * th + twist + i * 0.25);
                double rr = r * (1 + 0.16 * ridge);
                if (Math.hypot(dx, dz) > rr) continue;
                boolean outer = Math.hypot(dx, dz) > rr - 1.1;
                String block;
                if (u > 0.82) block = FANG_TIP;
                else if (i < 2) block = outer && rng.nextDouble() < 0.5 ? FANG_DIRT : FANG_BASE_ROCK;
                else if (outer && ridge > 0.7) block = FANG_RIDGE;
                else if (outer && u < 0.45 && rng.nextDouble() < 0.08) block = FANG_VEIN;
                else if (outer && u < 0.3 && rng.nextDouble() < 0.12) block = FANG_MOSS;
                else block = FANG_BODY;
                int x = (int) Math.floor(ox) + dx, z = (int) Math.floor(oz) + dz, y = y0 + i;
                cells.put(cellKey(x, y, z), new Cell(x, y, z, block));
            }
        }
        // Eboulis au pied
        for (int k = 0; k < (int) (baseRadius * 3); k++) {
            double a = rng.nextDouble(Math.PI * 2), d = baseRadius + rng.nextDouble(0.5, 2.0);
            int x = (int) Math.floor(base.x + Math.cos(a) * d), z = (int) Math.floor(base.z + Math.sin(a) * d);
            cells.putIfAbsent(cellKey(x, y0, z), new Cell(x, y0, z, rng.nextBoolean() ? FANG_BASE_ROCK : FANG_DIRT));
        }
        List<Cell> out = new ArrayList<>(cells.values());
        out.sort((a, b) -> Integer.compare(a.y(), b.y()));
        return out;
    }

    private static long cellKey(int x, int y, int z) {
        return (((long) x & 0x1FFFFF) << 42) | (((long) y & 0x1FFFFF) << 21) | ((long) z & 0x1FFFFF);
    }

    // ===================== TICK =====================

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Quake> list = quakes.get(uuid);
        if (list == null) return;
        long now = System.currentTimeMillis();
        for (Quake q : list) if (tickQuake(caster, q, now, store, cb)) list.remove(q);
        if (list.isEmpty()) quakes.remove(uuid);
    }

    private static boolean tickQuake(Ref<EntityStore> caster, Quake q, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        long t = now - q.startAt;
        if (q.terrain == null) q.terrain = TerrainRestore.open(world, q.startAt + RESTORE_AFTER_MS);
        if (q.waveSafety == null) q.waveSafety = TerrainRestore.open(world, q.startAt + 60_000);
        if (q.fangBatch == null) q.fangBatch = TerrainRestore.open(world, q.startAt + RESTORE_AFTER_MS);
        tickFangs(caster, q, t, now, world, store, cb);                                      // 1. crocs de la Terre
        if (!q.cracked) crack(q, world, store, cb);                                          // 2. fracture
        boolean anyLeft = q.plates.stream().anyMatch(p -> !p.slammed);
        if (anyLeft && now - q.lastGeyser >= GEYSER_EVERY_MS) geysers(caster, q, now, world, store, cb);
        int airborne = 0;
        for (Plate p : q.plates) if (p.airborne()) airborne += p.pieces.size();
        for (Plate p : q.plates) {
            if (!p.torn && t >= p.tearAt) airborne += tear(caster, q, p, Math.max(60, Math.min(PLATE_BUDGET, MAX_AIRBORNE - airborne)), world, store, cb);
        }
        if (now - q.lastClash >= CLASH_EVERY_MS) startClash(q, t, now);                     // 3. ballet et chocs
        tickClashes(caster, q, now, store, cb);
        if (now - q.lastDebrisFx >= DEBRIS_FX_EVERY_MS) {                                     // terre qui s'ecoule des plaques
            q.lastDebrisFx = now;
            for (Plate p : q.plates) if (p.airborne() && p.lift > 4) SpellFx.fx(FX_DUST, new Vector3d(p.center()).add(0, -1.5, 0), 1.6f, store, cb);
        }
        for (Plate p : q.plates) {
            if (!p.airborne()) continue;
            pose(p, t, now);
            if (t >= p.slamAt + SLAM_MS) slam(caster, q, p, now, world, store, cb);         // 4. retombee
            else place(p, store);
        }
        if (!anyLeft && !q.finalQuake) {                                                     // dernier seisme sur toute la zone
            q.finalQuake = true;
            q.waves.add(new Wave(new Vector3d(q.center), now, RADIUS, 1.5));
            SpellFx.fx(FX_EXPLOSION, new Vector3d(q.center).add(0, 1, 0), 10.0f, store, cb);
            SpellFx.fx(FX_DUST, new Vector3d(q.center).add(0, 1, 0), 10.0f, store, cb);
            SpellFx.fx(FX_SHOCKWAVE, new Vector3d(q.center).add(0, 0.2, 0), (float) (RADIUS / 2.0), store, cb);
            SpellFx.sound(SFX_SLAM, q.center, cb);
            SpellFx.sound(SFX_QUAKE, q.center, cb);
            SpellFx.shake(q.center, RADIUS * 4, "Impact_Strong", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, q.center, RADIUS + 10, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, q.center) > RADIUS) continue;
                Vector3d out = new Vector3d(p.x - q.center.x, 0, p.z - q.center.z);
                if (out.lengthSquared() < 1e-4) out.set(1, 0, 0);
                SpellFx.damage(caster, e, "Physical", FINAL_DAMAGE, out.normalize().mul(14).add(0, 9, 0), "Impact_Strong", cb);
                SpellFx.effect(e, STUN_EFFECT, cb);
            }
        }
        for (Wave w : q.waves) tickWave(caster, q, w, now, world, store, cb);
        q.waves.removeIf(w -> w.done >= w.maxRadius);
        tickLifted(q, now, world, store, cb);
        q.falling.removeIf(f -> {
            if (now < f.until()) return false;
            SpellFx.remove(f.ref(), cb);
            return true;
        });
        if (q.finalQuake && q.waves.isEmpty() && q.lifted.isEmpty()) {
            if (q.retractAt < 0) {                                                           // les crocs rentrent dans le sol
                q.retractAt = now;
                for (Fang f : q.fangs) if (!f.retracted) retract(q, f, world, store, cb);
                SpellFx.sound(SFX_QUAKE, q.center, cb);
            }
            if (now - q.retractAt < RETRACT_MS) return false;
            for (Falling f : q.falling) SpellFx.remove(f.ref(), cb);
            TerrainRestore.Restore batch = q.fangBatch;
            world.execute(() -> {
                try { TerrainRestore.restoreNow(world, batch); }
                catch (Exception e) { EldaniorLogger.error("GaiaAwakening: retrait des crocs", e); }
            });
            TerrainRestore.save();
            return true;
        }
        return false;
    }

    // ===================== CROCS =====================

    /** Les crocs jaillissent couche par couche (projetant les ennemis proches). */
    private static void tickFangs(Ref<EntityStore> caster, Quake q, long t, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        for (Fang f : q.fangs) {
            if (f.retracted || t < f.eruptAt) continue;
            if (!f.erupted) {
                f.erupted = true;
                SpellFx.fx(FX_DUST, new Vector3d(f.base).add(0, 0.5, 0), f.big ? 5.0f : 2.0f, store, cb);
                if (f.big) {
                    SpellFx.fx(FX_EXPLOSION, new Vector3d(f.base).add(0, 0.5, 0), 3.0f, store, cb);
                    SpellFx.sound(SFX_SLAM, f.base, cb);
                    SpellFx.shake(f.base, RADIUS * 1.5, "Impact_Strong", store);
                }
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, f.base, FANG_HIT_RADIUS + 2, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || SpellFx.horizontalDistance(p, f.base) > (f.big ? FANG_HIT_RADIUS : 2.0)) continue;
                    SpellFx.damage(caster, e, "Physical", FANG_DAMAGE, new Vector3d(0, FANG_LAUNCH, 0), "Impact_Strong", cb);
                    SpellFx.effect(e, STUN_EFFECT, cb);
                }
            }
            int target = (int) Math.min(f.cells.size(), f.cells.size() * Math.min(1.0, (t - f.eruptAt) / (double) FANG_RISE_MS));
            if (target > f.placed) {
                List<Cell> batch = new ArrayList<>(f.cells.subList(f.placed, target));
                f.placed = target;
                TerrainRestore.Restore restore = q.fangBatch;
                world.execute(() -> {
                    try {
                        for (Cell c : batch) if (placeInAir(world, restore, c.x(), c.y(), c.z(), c.block())) q.fangCells.add(cellKey(c.x(), c.y(), c.z()));
                    } catch (Exception e) {
                        EldaniorLogger.error("GaiaAwakening: crocs", e);
                    }
                });
            }
            if (f.retractAt >= 0 && t >= f.retractAt) retract(q, f, world, store, cb);
        }
    }

    /** Le croc rentre dans le sol (ses blocs sont retires, du haut vers le bas). */
    private static void retract(Quake q, Fang f, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        f.retracted = true;
        List<Cell> cells = new ArrayList<>(f.cells.subList(0, f.placed));
        SpellFx.fx(FX_DUST, new Vector3d(f.base).add(0, 0.5, 0), f.big ? 4.0f : 1.5f, store, cb);
        world.execute(() -> {
            try {
                for (int i = cells.size() - 1; i >= 0; i--) {
                    Cell c = cells.get(i);
                    long k = cellKey(c.x(), c.y(), c.z());
                    if (!q.fangCells.remove(k)) continue;
                    BlockType type = WorldBlockUtil.getBlockType(world, c.x(), c.y(), c.z());
                    if (type != null && c.block().equals(type.getId())) WorldBlockUtil.setBlock(world, c.x(), c.y(), c.z(), TerrainRestore.EMPTY);
                }
            } catch (Exception e) {
                EldaniorLogger.error("GaiaAwakening: retrait d'un croc", e);
            }
        });
    }

    // ===================== FAILLES ET GEYSERS =====================

    private static void crack(Quake q, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        q.cracked = true;
        List<Long> cracks = new ArrayList<>(q.cracks);
        TerrainRestore.Restore batch = q.terrain;
        int baseY = (int) Math.floor(q.center.y);
        world.execute(() -> {
            try { for (long k : cracks) crackColumn(world, batch, colX(k), baseY, colZ(k)); TerrainRestore.save(); }
            catch (Exception e) { EldaniorLogger.error("GaiaAwakening: fracture", e); }
        });
        for (Plate p : q.plates) SpellFx.fx(FX_DUST, new Vector3d(p.pivot).add(0, 0.5, 0), 3.0f, store, cb);
    }

    private static void geysers(Ref<EntityStore> caster, Quake q, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        q.lastGeyser = now;
        if (q.cracks.isEmpty()) return;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < GEYSERS_PER_PULSE; i++) {
            long k = q.cracks.get(rng.nextInt(q.cracks.size()));
            Vector3d at = SpellFx.ground(world, new Vector3d(colX(k) + 0.5, q.center.y, colZ(k) + 0.5));
            SpellFx.fx(FX_LAVA, new Vector3d(at).add(0, 0.5, 0), 2.5f, store, cb);
            SpellFx.fx(FX_STEAM, new Vector3d(at).add(0, 1, 0), 1.2f, store, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, GEYSER_RADIUS + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, at) > GEYSER_RADIUS) continue;
                SpellFx.damage(caster, e, "Fire", GEYSER_DAMAGE, new Vector3d(0, 7, 0), null, cb);
            }
        }
    }

    // ===================== PLAQUES =====================

    /** Arrache la plaque : sol (sur PLATE_DEPTH blocs), plantes et arbres ; retourne le nombre d'entites creees. */
    private static int tear(Ref<EntityStore> caster, Quake q, Plate p, int budget, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        p.torn = true;
        int baseY = (int) Math.floor(q.center.y);
        List<int[]> shown = new ArrayList<>(), hidden = new ArrayList<>(), vegetation = new ArrayList<>();
        double surfaceSum = 0;
        int surfaces = 0;
        for (long[] col : p.columns) {
            int x = (int) col[0], z = (int) col[1];
            if (TerrainRestore.isProtectedArea(world.getName(), x, baseY, z)) continue;
            int top = groundY(world, x, baseY, z);
            if (top == Integer.MIN_VALUE) continue;
            surfaceSum += top;
            surfaces++;
            for (int i = 0; i < PLATE_DEPTH; i++) (i == 0 || col[2] == 1 ? shown : hidden).add(new int[]{x, top - i, z});
            for (int y = top + 1; y <= top + VEGETATION_HEIGHT; y++) {
                if (TerrainRestore.isEmpty(world, x, y, z)) { if (y > top + 2) break; continue; }
                vegetation.add(new int[]{x, y, z});
            }
        }
        if (surfaces > 0) p.pivot.y = surfaceSum / surfaces + 1;
        shown.addAll(vegetation);
        List<int[]> cells = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        int spawned = 0;
        for (int[] c : shown) {
            String id = solidAt(world, c[0], c[1], c[2]);
            if (id == null) continue;
            cells.add(c);
            ids.add(id);
            if (spawned >= budget) continue;                                               // au-dela du budget : le bloc disparait
            Vector3d at = new Vector3d(c[0] + 0.5, c[1] + 0.5, c[2] + 0.5);
            Ref<EntityStore> ref = SpellFx.spawnBlock(id, at, store, cb);
            if (ref == null) continue;
            spawned++;
            p.pieces.add(new Piece(ref, new Vector3d(at).sub(p.pivot)));
            if (!id.contains("Leaves") && !id.startsWith("Wood_") && !id.startsWith("Plant_")) p.ground.add(id);
        }
        for (int[] c : hidden) {                                                            // l'interieur de la plaque part aussi
            String id = solidAt(world, c[0], c[1], c[2]);
            if (id != null) { cells.add(c); ids.add(id); }
        }
        TerrainRestore.Restore batch = q.terrain;
        world.execute(() -> {
            try {
                for (int i = 0; i < cells.size(); i++) {
                    int[] c = cells.get(i);
                    BlockType type = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                    if (type != null && ids.get(i).equals(type.getId())) TerrainRestore.place(world, batch, c[0], c[1], c[2], ids.get(i), TerrainRestore.EMPTY, 0);
                }
            } catch (Exception e) {
                EldaniorLogger.error("GaiaAwakening: arrachement", e);
            }
        });
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, p.pivot, 12, store)) {
            Vector3d pos = SpellFx.position(e, store);
            if (pos == null || Math.abs(pos.y - p.pivot.y) > 4 || !onPlate(p, pos)) continue;
            SpellFx.damage(caster, e, "Physical", TEAR_DAMAGE, new Vector3d(0, TEAR_LAUNCH, 0), "Impact_Strong", cb);
            SpellFx.effect(e, STUN_EFFECT, cb);
        }
        SpellFx.fx(FX_DUST, new Vector3d(p.pivot).add(0, 0.5, 0), 4.0f, store, cb);
        SpellFx.fx(FX_EXPLOSION, new Vector3d(p.pivot).add(0, 0.5, 0), 3.0f, store, cb);
        SpellFx.sound(SFX_QUAKE, p.pivot, cb);
        SpellFx.shake(p.pivot, RADIUS * 1.5, "Impact_Strong", store);
        return spawned;
    }

    private static boolean onPlate(Plate p, Vector3d pos) {
        int x = (int) Math.floor(pos.x), z = (int) Math.floor(pos.z);
        for (long[] c : p.columns) if (c[0] == x && c[1] == z) return true;
        return false;
    }

    private static String solidAt(World world, int x, int y, int z) {
        BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
        if (type == null || TerrainRestore.isEmpty(world, x, y, z)) return null;
        String id = type.getId();
        if (TerrainRestore.isKept(world, id, x, y, z) || WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) return null;
        return id;
    }

    /** Pose de la plaque : montee en basculant, orbite et tangage, retournement, puis chute. */
    private static void pose(Plate p, long t, long now) {
        double since = t - p.tearAt;
        double rise = Math.min(1.0, since / RISE_MS), ease = 1 - Math.pow(1 - rise, 3);
        double bob = rise >= 1 ? Math.sin(now / 650.0 + p.phase) * 0.8 : 0;
        long flipAt = p.slamAt - FLIP_MS, orbitFrom = p.tearAt + RISE_MS / 2;
        double run = t <= orbitFrom ? 0 : ORBIT_SPEED * p.orbitDir * (Math.min(t, flipAt) - orbitFrom) / 1000.0;
        p.lift = p.alt * ease + bob;
        p.pitch = p.tiltPitch * ease + Math.sin(now / 900.0 + p.phase) * 0.08 * ease;
        p.roll = p.tiltRoll * ease;
        p.yaw = p.spin * since / 1000.0;
        p.orbit = run;
        if (t >= flipAt) {                                                                   // retournement au-dessus de son trou
            double f = Math.min(1.0, (t - flipAt) / (double) FLIP_MS), fe = f * f * (3 - 2 * f);
            p.lift = p.alt + bob * (1 - fe) + FLIP_LIFT * fe;
            p.pitch = p.tiltPitch * (1 - fe) + Math.PI * fe;
            p.roll = p.tiltRoll * (1 - fe);
            p.orbit = run * (1 - fe);
            p.drift.mul(1 - fe * 0.2);
        }
        if (t >= p.slamAt) {                                                                 // chute brutale
            double s = Math.min(1.0, (t - p.slamAt) / (double) SLAM_MS);
            p.lift = (p.alt + FLIP_LIFT) * (1 - s * s) - 0.5 * s;
            p.drift.mul(1 - s);
            p.orbit = 0;
            p.pitch = Math.PI;
            p.roll = 0;
        }
    }

    /** Place chaque bloc : pivot + rotation (lacet, tangage, roulis) de sa position relative. */
    private static void place(Plate p, Store<EntityStore> store) {
        Vector3d c = p.center();
        double yaw = p.yaw - p.orbit;
        double cy = Math.cos(yaw), sy = Math.sin(yaw), cp = Math.cos(p.pitch), sp = Math.sin(p.pitch), cr = Math.cos(p.roll), sr = Math.sin(p.roll);
        Rotation3f rot = new Rotation3f();
        rot.setYaw((float) yaw);
        rot.setPitch((float) p.pitch);
        rot.setRoll((float) p.roll);
        for (Piece pc : p.pieces) {
            Vector3d l = pc.local();
            double x1 = l.x * cr - l.y * sr, y1 = l.x * sr + l.y * cr, z1 = l.z;
            double y2 = y1 * cp - z1 * sp, z2 = y1 * sp + z1 * cp;
            double x3 = x1 * cy + z2 * sy, z3 = -x1 * sy + z2 * cy;
            SpellFx.hold(pc.ref(), new Vector3d(c.x + x3, c.y + y2, c.z + z3), rot, store);
        }
    }

    /** Lance un choc entre deux plaques en vol (les plus proches), qui ne se percutent pas deja. */
    private static void startClash(Quake q, long t, long now) {
        List<Plate> free = new ArrayList<>();
        for (Plate p : q.plates) {
            if (!p.airborne() || t < p.tearAt + RISE_MS || t > p.slamAt - FLIP_MS - CLASH_MS) continue;
            boolean busy = false;
            for (Clash c : q.clashes) if (c.a() == p || c.b() == p) { busy = true; break; }
            if (!busy) free.add(p);
        }
        if (free.size() < 2) return;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Plate a = free.remove(rng.nextInt(free.size())), b = null;
        double best = Double.MAX_VALUE;
        for (Plate o : free) {
            double d = a.center().distance(o.center());
            if (d < best) { best = d; b = o; }
        }
        if (b == null) return;
        q.clashes.add(new Clash(a, b, now, Math.max(0, (SpellFx.horizontalDistance(a.pivot, b.pivot) - 12) / 2)));
        q.lastClash = now;
    }

    private static void tickClashes(Ref<EntityStore> caster, Quake q, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        q.clashes.removeIf(c -> {
            double f = (now - c.startAt()) / (double) CLASH_MS;
            Vector3d dir = new Vector3d(c.b().pivot.x - c.a().pivot.x, 0, c.b().pivot.z - c.a().pivot.z);
            if (dir.lengthSquared() > 1e-6) dir.normalize();
            double k = c.close() * Math.sin(Math.PI * Math.min(1, f));
            c.a().drift.set(dir.x * k, 0, dir.z * k);
            c.b().drift.set(-dir.x * k, 0, -dir.z * k);
            if (f >= 0.5 && q.clashHit.add(c)) {
                Vector3d mid = new Vector3d(c.a().center()).add(c.b().center()).mul(0.5);
                SpellFx.fx(FX_EXPLOSION, mid, 5.0f, store, cb);
                SpellFx.fx(FX_DUST, mid, 5.0f, store, cb);
                SpellFx.fx(FX_SHOCKWAVE, mid, 2.0f, store, cb);
                SpellFx.sound(SFX_SLAM, mid, cb);
                SpellFx.sound(SFX_QUAKE, mid, cb);
                SpellFx.shake(mid, RADIUS * 2, "Impact_Strong", store);
                breakOff(q, c.a(), rng, now);
                breakOff(q, c.b(), rng, now);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, mid, CLASH_RADIUS, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null) continue;
                    Vector3d out = new Vector3d(p).sub(mid);
                    out.y = 0;
                    if (out.lengthSquared() < 1e-4) out.set(1, 0, 0);
                    SpellFx.damage(caster, e, "Physical", CLASH_DAMAGE, out.normalize().mul(10).add(0, 5, 0), "Impact_Strong", cb);
                }
            }
            if (f >= 1) {
                c.a().drift.set(0, 0, 0);
                c.b().drift.set(0, 0, 0);
                q.clashHit.remove(c);
                return true;
            }
            return false;
        });
    }

    private static void breakOff(Quake q, Plate p, ThreadLocalRandom rng, long now) {
        for (int i = 0; i < BREAK_OFF && !p.pieces.isEmpty(); i++) {
            Piece pc = p.pieces.remove(rng.nextInt(p.pieces.size()));
            q.falling.add(new Falling(pc.ref(), now + FALLING_LIFE_MS));
        }
    }

    /** La plaque s'ecrase : cratere, decombres, degats enormes dessous, et un tremblement de terre part de l'impact. */
    private static void slam(Ref<EntityStore> caster, Quake q, Plate p, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        p.slammed = true;
        for (Piece pc : p.pieces) SpellFx.remove(pc.ref(), cb);
        p.pieces.clear();
        Vector3d at = new Vector3d(p.pivot);
        SpellFx.fx(FX_EXPLOSION, new Vector3d(at).add(0, 1, 0), 8.0f, store, cb);
        SpellFx.fx(FX_DUST, new Vector3d(at).add(0, 1, 0), 8.0f, store, cb);
        SpellFx.fx(FX_SHOCKWAVE, new Vector3d(at).add(0, 0.2, 0), 4.0f, store, cb);
        SpellFx.sound(SFX_SLAM, at, cb);
        SpellFx.sound(SFX_QUAKE, at, cb);
        SpellFx.shake(at, RADIUS * 3, "Impact_Strong", store);
        q.waves.add(new Wave(at, now, IMPACT_QUAKE_RADIUS, 1.0));
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, 16, store)) {
            Vector3d pos = SpellFx.position(e, store);
            if (pos == null || !(onPlate(p, pos) || SpellFx.horizontalDistance(pos, at) <= SLAM_EXTRA + 3)) continue;
            Vector3d out = new Vector3d(pos.x - at.x, 0, pos.z - at.z);
            if (out.lengthSquared() < 1e-4) out.set(1, 0, 0);
            SpellFx.damage(caster, e, "Physical", SLAM_DAMAGE, out.normalize().mul(14).add(0, 8, 0), "Impact_Strong", cb);
            SpellFx.effect(e, STUN_EFFECT, cb);
        }
        List<long[]> columns = new ArrayList<>(p.columns);
        List<String> rubble = new ArrayList<>(p.ground);
        TerrainRestore.Restore batch = q.terrain;
        int baseY = (int) Math.floor(q.center.y);
        world.execute(() -> {
            try { crater(world, batch, columns, rubble, baseY); }
            catch (Exception e) { EldaniorLogger.error("GaiaAwakening: impact", e); }
        });
    }

    // ===================== TREMBLEMENTS DE TERRE =====================

    /** Le front avance : sur l'anneau atteint, un bloc de surface sur deux se souleve ; les ennemis du front sont projetes. */
    private static void tickWave(Ref<EntityStore> caster, Quake q, Wave w, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        double r = Math.min(w.maxRadius, WAVE_SPEED * (now - w.startAt) / 1000.0);
        if (r <= w.done) return;
        double from = w.done;
        w.done = r;
        List<int[]> cells = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        int R = (int) Math.ceil(r) + 1, cx = (int) Math.floor(w.origin.x), cz = (int) Math.floor(w.origin.z), cy = (int) Math.floor(q.center.y);
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int dx = -R; dx <= R; dx++) {
            for (int dz = -R; dz <= R; dz++) {
                double d = Math.hypot(dx, dz);
                if (d <= from || d > r || ((dx + dz) & 1) != 0 || q.lifted.size() + cells.size() >= MAX_LIFTED) continue;
                int x = cx + dx, z = cz + dz;
                if (TerrainRestore.isProtectedArea(world.getName(), x, cy, z)) continue;
                int y = groundY(world, x, cy, z);
                if (y == Integer.MIN_VALUE || q.fangCells.contains(cellKey(x, y, z)) || !q.liftedCells.add(colKey(x, z) ^ ((long) y << 52))) continue;
                String id = solidAt(world, x, y, z);
                if (id == null || id.startsWith("Wood_") || WorldBlockUtil.getFluidIndex(world, x, y + 1, z) > 0) continue;
                double height = LIFT_HEIGHT * w.power * (1 - 0.45 * d / w.maxRadius) * rng.nextDouble(0.8, 1.2);
                Ref<EntityStore> ref = SpellFx.spawnBlock(id, new Vector3d(x + 0.5, y + 0.5, z + 0.5), store, cb);
                if (ref == null) continue;
                q.lifted.add(new Lifted(ref, x, y, z, id, Math.max(0, WorldBlockUtil.getRotationIndex(world, x, y, z)), height, now));
                cells.add(new int[]{x, y, z});
                ids.add(id);
            }
        }
        if (!cells.isEmpty()) {
            TerrainRestore.Restore safety = q.waveSafety;
            world.execute(() -> {
                try {
                    for (int i = 0; i < cells.size(); i++) {
                        int[] c = cells.get(i);
                        BlockType type = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                        if (type != null && ids.get(i).equals(type.getId())) TerrainRestore.place(world, safety, c[0], c[1], c[2], ids.get(i), TerrainRestore.EMPTY, 0);
                    }
                } catch (Exception e) {
                    EldaniorLogger.error("GaiaAwakening: seisme", e);
                }
            });
        }
        if (rng.nextInt(3) == 0) {
            double a = rng.nextDouble(Math.PI * 2);
            SpellFx.fx(FX_DUST, SpellFx.ground(world, new Vector3d(w.origin).add(Math.cos(a) * r, 0, Math.sin(a) * r)).add(0, 0.5, 0), 1.5f, store, cb);
        }
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, w.origin, r + WAVE_BAND + 2, store)) {
            if (w.hit.contains(e)) continue;
            Vector3d p = SpellFx.position(e, store);
            if (p == null || Math.abs(SpellFx.horizontalDistance(p, w.origin) - r) > WAVE_BAND) continue;
            w.hit.add(e);
            Vector3d push = new Vector3d(p.x - w.origin.x, 0, p.z - w.origin.z);
            if (push.lengthSquared() < 1e-4) push.set(1, 0, 0);
            push.normalize().mul(WAVE_PUSH * w.power).add(0, WAVE_LAUNCH * w.power, 0);
            SpellFx.damage(caster, e, "Physical", (float) (WAVE_DAMAGE * w.power), push, "Impact_Strong", cb);
            SpellFx.effect(e, STUN_EFFECT, cb);
        }
    }

    /** Blocs souleves : montent et retombent, puis reprennent leur place dans le sol. */
    private static void tickLifted(Quake q, long now, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (q.lifted.isEmpty()) return;
        List<Lifted> landed = new ArrayList<>();
        q.lifted.removeIf(l -> {
            double p = (now - l.startAt()) / (double) LIFT_MS;
            if (p >= 1) { landed.add(l); SpellFx.remove(l.ref(), cb); return true; }
            Rotation3f tilt = new Rotation3f();
            tilt.setPitch((float) (Math.sin(Math.PI * p) * 0.25 * ((l.x() + l.z()) % 3 - 1)));
            SpellFx.hold(l.ref(), new Vector3d(l.x() + 0.5, l.y() + 0.5 + l.height() * Math.sin(Math.PI * p), l.z() + 0.5), tilt, store);
            return false;
        });
        if (landed.isEmpty()) return;
        world.execute(() -> {
            try {
                for (Lifted l : landed) {                                                       // reenregistre : la restauration le retrouvera
                    if (TerrainRestore.isEmpty(world, l.x(), l.y(), l.z())) TerrainRestore.place(world, q.terrain, l.x(), l.y(), l.z(), TerrainRestore.EMPTY, l.block(), l.rotation());
                    q.liftedCells.remove(colKey(l.x(), l.z()) ^ ((long) l.y() << 52));
                }
            } catch (Exception e) {
                EldaniorLogger.error("GaiaAwakening: retombee du seisme", e);
            }
        });
    }

    // ===================== TERRAIN (hors tick ECS) =====================

    /** Faille : la surface s'ouvre sur CRACK_DEPTH blocs, lave au fond sur de la roche fendue de lave. */
    private static void crackColumn(World world, TerrainRestore.Restore batch, int x, int baseY, int z) {
        if (TerrainRestore.isProtectedArea(world.getName(), x, baseY, z)) return;
        int top = groundY(world, x, baseY, z);
        if (top == Integer.MIN_VALUE) return;
        int bottom = top - CRACK_DEPTH + 1;
        for (int y = top + 3; y >= bottom; y--) {
            BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
            if (type == null || TerrainRestore.isEmpty(world, x, y, z)) continue;
            String id = type.getId();
            if (TerrainRestore.isKept(world, id, x, y, z) || id.startsWith("Wood_") || WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) return;
            TerrainRestore.place(world, batch, x, y, z, id, TerrainRestore.EMPTY, 0);
        }
        BlockType floor = WorldBlockUtil.getBlockType(world, x, bottom - 1, z);
        if (floor != null && !TerrainRestore.isEmpty(world, x, bottom - 1, z) && !TerrainRestore.isKept(world, floor.getId(), x, bottom - 1, z)) {
            TerrainRestore.place(world, batch, x, bottom - 1, z, floor.getId(), CRACK_FLOOR, 0);
        }
        if (TerrainRestore.isEmpty(world, x, bottom, z)) TerrainRestore.placeFluid(world, batch, x, bottom, z, LAVA);
    }

    /** Sous la plaque ecrasee : le fond est enfonce de CRATER_DEPTH blocs, des decombres de la plaque le jonchent. */
    private static void crater(World world, TerrainRestore.Restore batch, List<long[]> columns, List<String> rubble, int baseY) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (long[] col : columns) {
            int x = (int) col[0], z = (int) col[1];
            if (TerrainRestore.isProtectedArea(world.getName(), x, baseY, z)) continue;
            int top = groundY(world, x, baseY, z);
            if (top == Integer.MIN_VALUE) continue;
            int removed = 0;
            for (int y = top; removed < CRATER_DEPTH; y--, removed++) {
                String id = solidAt(world, x, y, z);
                if (id == null) break;
                TerrainRestore.place(world, batch, x, y, z, id, TerrainRestore.EMPTY, 0);
            }
            if (!rubble.isEmpty() && rng.nextDouble() < RUBBLE_CHANCE) {
                int y = top - removed + 1;
                if (TerrainRestore.isEmpty(world, x, y, z)) TerrainRestore.place(world, batch, x, y, z, TerrainRestore.EMPTY, rubble.get(rng.nextInt(rubble.size())), 0);
            }
        }
        TerrainRestore.save();
    }

    private static boolean placeInAir(World world, TerrainRestore.Restore batch, int x, int y, int z, String block) {
        if (TerrainRestore.isProtectedArea(world.getName(), x, y, z)) return false;
        BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
        if (type == null) return false;
        boolean empty = TerrainRestore.isEmpty(world, x, y, z);
        if (!empty && !type.getId().startsWith("Plant_")) return false;
        if (WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) return false;
        return TerrainRestore.place(world, batch, x, y, z, empty ? TerrainRestore.EMPTY : type.getId(), block, 0);
    }

    /** Premier bloc de sol (hors plantes, feuilles et bois) en descendant, ou MIN_VALUE. */
    private static int groundY(World world, int x, int baseY, int z) {
        for (int y = baseY + SCAN_UP; y >= baseY - SCAN_DOWN; y--) {
            BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
            if (type == null) return Integer.MIN_VALUE;
            if (TerrainRestore.isEmpty(world, x, y, z)) continue;
            String id = type.getId();
            if (id.startsWith("Plant_") || id.contains("Leaves") || id.startsWith("Wood_")) continue;
            return y;
        }
        return Integer.MIN_VALUE;
    }

    private static long colKey(int x, int z) { return ((long) x << 32) | (z & 0xffffffffL); }
    private static int colX(long k) { return (int) (k >> 32); }
    private static int colZ(long k) { return (int) k; }

    public static void handleDisconnect(UUID uuid) {
        if (quakes.remove(uuid) != null) TerrainRestore.save();
    }
}
