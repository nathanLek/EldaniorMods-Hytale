package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Eveil d'Yggdrasil — sort divin de nature (Rune_EveilYggdrasil), le frene-monde nordique.
 *
 * Une graine tombe sur la zone visee : la nature reprend TOUT le sol de la zone ({@link #RADIUS} blocs, comme la
 * Porte de Niflheim), quel que soit le bloc, en une onde qui part du centre. Tant que l'arbre existe
 * ({@link #DURATION_MS}) :
 *  - au centre, l'arbre-monde (~{@link #TREE_HEIGHT} blocs : pied evase a contreforts, racines qui serpentent au sol,
 *    tronc cannele en spirale, charpentieres courbes qui se ramifient trois fois, houppier en grappes parseme d'or,
 *    lianes) se dresse au-dessus d'un sanctuaire de marbre a colonnes de cristaux. Sous le tronc, une salle voutee
 *    tapissee de bois et de racines abrite la fontaine de vie ; on y descend par trois galeries qui suivent les
 *    grandes racines et debouchent dans le jardin. Le jardin fleuri benit les allies (vie, endurance, mana, -20 % de
 *    degats subis) ; dans la salle de la fontaine, ils sont en plus purifies et soignes davantage ;
 *  - l'anneau du milieu : pieges vegetaux (racines qui immobilisent et empoisonnent), ronces qui ralentissent et une
 *    vague de poison qui le traverse regulierement ;
 *  - l'anneau exterieur : une foret a traverser, noyee dans une brume toxique verte au ras du sol.
 * Juste avant que l'arbre disparaisse, il libere une onde qui projette les ennemis. Tout est restaure, puis un
 * dernier passage efface ce qui resterait (lianes, feuilles...).
 */
public final class YggdrasilAwakening {

    private YggdrasilAwakening() {}

    // ===== Reglages =====
    private static final double TARGET_RANGE = 40.0, FALLBACK_DISTANCE = 14.0;
    static final double RADIUS = SpellFx.DIVINE_ZONE_RADIUS;
    /** Jardin et sanctuaire (au pied de l'arbre) | pieges et poison | foret et brume. */
    static final double GARDEN_RADIUS = 17.5, TRAP_RADIUS = 26.5;
    private static final double SPREAD_SPEED = 12.0;                  // blocs/s : la nature gagne la zone en ~3 s
    private static final int SCAN_UP = 14, SCAN_DOWN = 10;
    static final long RESTORE_AFTER_MS = 180_000;                     // l'arbre disparait (terrain restaure)
    private static final long GROW_MS = 12_000, EFFECTS_FROM_MS = 2_000;
    private static final long DURATION_MS = RESTORE_AFTER_MS - 3_000; // les effets durent tant que l'arbre existe
    private static final long SWEEP_AFTER_MS = RESTORE_AFTER_MS + 20_000;
    // Arbre-monde
    static final int TREE_HEIGHT = 100;
    private static final int SPLIT_HEIGHT = 48;                        // fourche ou partent les charpentieres
    // Sanctuaire
    private static final double PLAZA_INNER = 11.0, PLAZA_OUTER = 14.5, COLUMN_RING = 15.5;
    private static final int COLUMNS = 8;
    private static final float FOUNTAIN_HEAL = 0.05f, FOUNTAIN_MANA = 0.05f;
    // Salle de la fontaine (sous le tronc) et galeries qui y descendent en suivant les racines
    private static final int HALL_DEPTH = 8;                          // sol de la salle : 8 blocs sous la surface
    private static final double HALL_RADIUS = 7.5, HALL_HEIGHT = 6.5, BASIN_RADIUS = 3.5;
    private static final int[] GALLERY_ROOTS = {0, 3, 6};             // contreforts sous lesquels passent les galeries
    private static final double GALLERY_MOUTH = 19.5, GALLERY_HALF_WIDTH = 1.5;
    private static final int GALLERY_HEADROOM = 3;
    // Foret
    private static final double FOREST_SPACING = 4.6;
    // Jardin : benediction de l'arbre-monde
    private static final float BLESS_HEAL = 0.03f, BLESS_STAMINA = 0.04f, BLESS_MANA = 0.03f;
    private static final float BLESS_DAMAGE_TAKEN = 0.8f;
    // Anneau du milieu : pieges vegetaux et vague de poison
    private static final int TRAP_COUNT = 16;
    private static final double TRAP_TRIGGER = 1.8;
    private static final long TRAP_ROOT_MS = 3_000, TRAP_REARM_MS = 7_000, TRAP_FX_EVERY_MS = 1_500;
    private static final float TRAP_DAMAGE = 120f;
    private static final long WAVE_EVERY_MS = 6_000, WAVE_TRAVEL_MS = 1_600;
    private static final double WAVE_WIDTH = 1.8;
    private static final float WAVE_DAMAGE = 80f;
    // Brume toxique (anneau exterieur)
    private static final double MIST_SPACING = 5.0;
    private static final long MIST_EVERY_MS = 9_000, POISON_EVERY_MS = 2_000, FOUNTAIN_FX_EVERY_MS = 2_000;
    // Onde finale
    private static final float FINAL_DAMAGE = 800f;
    private static final double FINAL_PUSH = 16.0, FINAL_LIFT = 8.0;

    // ===== Blocs =====
    private static final String TRUNK = "Wood_Ash_Trunk", ROOTS = "Wood_Ash_Roots", MOSS = "Plant_Moss_Block_Green";
    private static final String LEAVES = "Plant_Leaves_Ash", GOLD_LEAVES = "Plant_Leaves_Goldentree";
    private static final String LIANA = "Plant_Vine_Liana", LIANA_END = "Plant_Vine_Liana_End", TRAP_BLOCK = "Plant_Bramble_Moss_Twisted";
    private static final String MARBLE = "Rock_Marble_Brick_Smooth", MARBLE_ALT = "Rock_Calcite_Brick_Smooth", MARBLE_BORDER = "Rock_Marble_Brick_Ornate";
    private static final String BASIN_RIM = "Rock_Marble_Brick_Half", PILLAR_BASE = "Rock_Marble_Brick_Pillar_Base", PILLAR = "Rock_Marble_Brick_Pillar_Middle";
    private static final String CRYSTAL = "Rock_Crystal_Green_Medium", BIG_CRYSTAL = "Rock_Crystal_Green_Large", WATER = "Water_Source";
    private static final String[] GROUND = {"Soil_Grass_Sunny_Full", "Soil_Grass_Wet_Full", "Soil_Grass_Deep_Full"};   // jardin, pieges, foret
    private static final String[] FLOWERS = {"Plant_Flower_Common_Blue", "Plant_Flower_Common_Pink", "Plant_Flower_Common_White",
            "Plant_Flower_Common_Yellow", "Plant_Flower_Common_Violet", "Plant_Flower_Bushy_Red", "Plant_Flower_Bushy_Orange", "Plant_Flower_Bushy_Cyan"};
    private static final String[] POISON_PLANTS = {"Plant_Flower_Bushy_Poisoned", "Plant_Flower_Common_Poisoned", "Plant_Moss_Rug_Lime", "Plant_Grass_Lush_Tall"};
    private static final String[] FOREST_FLOOR = {"Plant_Fern_Forest", "Plant_Fern", "Plant_Grass_Lush_Tall", "Plant_Bush_Green", "Plant_Moss_Rug_Green"};
    private static final String[][] FOREST_TREES = {   // tronc, feuillage
            {"Wood_Oak_Trunk", "Plant_Leaves_Oak"}, {"Wood_Birch_Trunk", "Plant_Leaves_Birch"}, {"Wood_Beech_Trunk", "Plant_Leaves_Beech"},
            {"Wood_Maple_Trunk", "Plant_Leaves_Maple"}, {"Wood_Fir_Trunk", "Plant_Leaves_Fir"}};
    /** Blocs propres au sort : le passage final efface ceux qui resteraient la ou il n'y avait que de l'air. */
    private static final String[] SPELL_BLOCKS = {"Wood_Ash", "Plant_Leaves", "Plant_Vine", "Plant_Moss", "Plant_Flower", "Plant_Grass_Lush",
            "Plant_Fern", "Plant_Bush", "Plant_Bramble", "Rock_Marble", "Rock_Crystal_Green", "Wood_Oak", "Wood_Birch", "Wood_Beech", "Wood_Maple", "Wood_Fir"};

    // ===== Assets =====
    private static final String ROOT_EFFECT = "Yggdrasil_Racines";
    private static final String THORN_EFFECT = "Yggdrasil_Ronces";
    private static final String POISON_EFFECT = "Yggdrasil_Poison";
    private static final String BLESS_EFFECT = "Yggdrasil_Benediction";
    private static final String FX_LEAVES = "Block_Break_Leaves";
    private static final String FX_MIST = "Yggdrasil_Brume";
    private static final String FX_WAVE = "Yggdrasil_Wave";
    private static final String FX_TRAP = "Yggdrasil_Trap";
    private static final String FX_FOUNTAIN = "Yggdrasil_Fountain";
    private static final String FX_SPORES = "Yggdrasil_Spores";
    private static final String FX_SEED = "Gilgamesh_Sword_Burst";
    private static final String FX_EXPLOSION = "Explosion_Big";
    private static final String SFX_GROW = "SFX_Deployable_Totem_Heal_Spawn";
    private static final String SFX_ROOT = "SFX_Emit_Wind_Grass";
    private static final String SFX_FOUNTAIN = "SFX_Health_Potion_High_Drink";
    private static final String SFX_FINAL = "SFX_Golem_Frost_Slam_Impact";

    record Cell(int x, int y, int z, String block) {}
    /** Arbre de la foret : position polaire autour du centre, essence, graine. */
    record Sapling(double angle, double dist, int kind, long seed) {}

    static final class Trap {
        final Vector3d pos;
        long armedAt;
        Trap(Vector3d pos) { this.pos = pos; }
    }

    static final class Grove {
        final World world;
        final Vector3d center;
        final long startAt;
        final List<Cell> tree = new ArrayList<>();
        final List<Sapling> forest = new ArrayList<>();
        final List<Trap> traps = new ArrayList<>();
        final List<Vector3d> fountains = new ArrayList<>();
        final List<Double> rootAngles = new ArrayList<>();
        /** Salle et galeries : case -> bloc a poser (EMPTY = a creuser). */
        final Map<Long, String> burrow = new HashMap<>();
        /** Colonnes (x, z) ou la salle ou les galeries affleurent : le sol de surface n'y est pas transforme. */
        final Set<Long> burrowColumns = new HashSet<>();
        final Set<Long> water = new HashSet<>();
        int hallFloor;
        final Set<Ref<EntityStore>> waveHit = new HashSet<>();
        /** Cases ou le sort a pose un bloc dans l'air (passage de nettoyage final). */
        final Set<Long> placedInAir = new HashSet<>();
        int placed, planted;
        double spreadRadius;
        long lastPulse, lastPoison, lastMist, lastTrapFx, lastWave, waveAt = -1, lastWaveFx, lastLeaves, lastFountainFx;
        boolean opened, finished, swept;
        TerrainRestore.Restore restore;
        Grove(World world, Vector3d center, long startAt) { this.world = world; this.center = center; this.startAt = startAt; }
    }

    private static final Map<UUID, List<Grove>> groves = new ConcurrentHashMap<>();
    /** Allies benis -> fin de la benediction (lu par DivineDamageSystem). */
    private static final Map<Ref<EntityStore>, Long> blessedUntil = new ConcurrentHashMap<>();

    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        if (uuid == null) return;
        World world = SpellFx.world(store);
        Vector3d center = SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK_DISTANCE);
        Grove g = new Grove(world, center, System.currentTimeMillis());
        planTree(g, ThreadLocalRandom.current().nextLong());
        planBurrow(g, world);
        planForest(g);
        planTraps(g, world);
        groves.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(g);
    }

    /** Benediction de l'arbre-monde : -20 % de degats subis (appele par DivineDamageSystem). */
    public static float damageTakenMultiplier(Ref<EntityStore> target) {
        if (blessedUntil.isEmpty() || target == null) return 1f;
        Long until = blessedUntil.get(target);
        return until != null && System.currentTimeMillis() < until ? BLESS_DAMAGE_TAKEN : 1f;
    }

    // ===================== PLAN DE L'ARBRE-MONDE =====================

    /** Bloc de l'arbre en coordonnees relatives (au pied du tronc, y = 0 au niveau du sol). */
    private static final class TreePlan {
        final Map<Long, Cell> cells = new HashMap<>();
        final int cx, cy, cz;
        final Random rng;
        TreePlan(int cx, int cy, int cz, Random rng) { this.cx = cx; this.cy = cy; this.cz = cz; this.rng = rng; }
        void put(double x, double y, double z, String block, boolean override) {
            Cell c = new Cell(cx + (int) Math.round(x), cy + (int) Math.round(y), cz + (int) Math.round(z), block);
            long k = key(c.x(), c.y(), c.z());
            if (override) cells.put(k, c); else cells.putIfAbsent(k, c);
        }
        boolean has(double x, double y, double z) {
            return cells.containsKey(key(cx + (int) Math.round(x), cy + (int) Math.round(y), cz + (int) Math.round(z)));
        }
    }

    private static double[] trunkCenter(double y) {
        return new double[]{1.4 * Math.sin(y / 30.0), 1.1 * Math.sin(y / 23.0 + 1) - 1.1 * Math.sin(1)};
    }

    /** Rayon du tronc : s'affine jusqu'a la fourche, pied tres evase. */
    private static double trunkRadius(double y) {
        double r = 3.6 + 3.0 * Math.pow(1 - Math.min(1, y / SPLIT_HEIGHT), 1.3);
        return r + Math.pow(Math.max(0, 7 - y), 1.6) * 0.16;
    }

    /**
     * Arbre-monde inspire des grands chenes et de l'Yggdrasil des illustrations : tronc cannele en spirale (coquille
     * de 2,5 blocs), contreforts en aileron, racines qui serpentent et se fourchent au sol, 4 charpentieres basses et
     * 5 a la fourche + une fleche centrale, chacune ramifiee deux fois (port en candelabre), houppier en grappes
     * irregulieres (feuilles d'or), lianes sous les branches, mousse sur l'ecorce.
     */
    private static void planTree(Grove g, long seed) {
        TreePlan t = new TreePlan((int) Math.floor(g.center.x), (int) Math.floor(g.center.y), (int) Math.floor(g.center.z), new Random(seed));
        Random rng = t.rng;
        // Tronc cannele
        for (int y = 0; y < SPLIT_HEIGHT + 6; y++) {
            double[] c = trunkCenter(y);
            double R = trunkRadius(y);
            int B = (int) Math.ceil(R * 1.2) + 1;
            for (int dx = -B; dx <= B; dx++) for (int dz = -B; dz <= B; dz++) {
                double px = dx - c[0], pz = dz - c[1], th = Math.atan2(pz, px), d = Math.hypot(px, pz);
                double rr = R * (1 + 0.14 * Math.sin(6 * th + y * 0.09) + 0.06 * Math.sin(11 * th - y * 0.05));
                if (d > rr || d <= rr - 2.6) continue;
                boolean bark = d > rr - 1 && rng.nextDouble() < 0.06 && y < SPLIT_HEIGHT - 4;
                t.put(dx, y, dz, bark ? MOSS : TRUNK, true);
            }
        }
        // Contreforts et racines de surface
        for (int k = 0; k < 9; k++) {
            double a = 2 * Math.PI * k / 9 + rng.nextDouble(-0.2, 0.2), ca = Math.cos(a), sa = Math.sin(a);
            g.rootAngles.add(a);
            double[] top = {ca * 3, 10 + rng.nextDouble(-1, 2), sa * 3}, foot = {ca * 10.5, 0.5, sa * 10.5};
            for (int i = 0; i <= 6; i++) {
                double s = i / 6.0;
                double[] p = {top[0] + (foot[0] - top[0]) * s, top[1] + (foot[1] - top[1]) * s, top[2] + (foot[2] - top[2]) * s};
                capsule(t, TRUNK, new double[]{ca * 2, p[1], sa * 2}, p, 1.5, 1.3 - 0.5 * s, true);
            }
            double[] p = foot;
            double ang = a, r = 1.8;
            for (int s = 0; s < 7; s++) {
                ang += rng.nextDouble(-0.35, 0.35);
                double[] q = {p[0] + Math.cos(ang) * 2.4, Math.max(-1.2, p[1] - 0.25), p[2] + Math.sin(ang) * 2.4};
                capsule(t, ROOTS, p, q, r, r * 0.88, true);
                p = q;
                r *= 0.88;
                if (s == 2 && rng.nextDouble() < 0.7) {                                   // fourche
                    double bAng = ang + (rng.nextBoolean() ? 1 : -1) * rng.nextDouble(0.6, 1.0), br = r * 0.7;
                    double[] bp = p;
                    for (int j = 0; j < 3; j++) {
                        double[] bq = {bp[0] + Math.cos(bAng) * 2.2, bp[1] - 0.25, bp[2] + Math.sin(bAng) * 2.2};
                        capsule(t, ROOTS, bp, bq, br, br * 0.85, true);
                        bp = bq;
                        br *= 0.85;
                        bAng += rng.nextDouble(-0.3, 0.3);
                    }
                }
            }
        }
        // Charpentieres
        List<double[]> clumps = new ArrayList<>();
        List<double[]> lianas = new ArrayList<>();
        for (int k = 0; k < 4; k++) {
            double y = 30 + k * 4;
            double[] c = trunkCenter(y);
            double az = k * 2.4 + rng.nextDouble(-0.3, 0.3);
            branch(t, new double[]{c[0] + Math.cos(az) * 2, y, c[1] + Math.sin(az) * 2}, az, Math.toRadians(rng.nextDouble(16, 24)), rng.nextDouble(24, 28), 2.6, 0, clumps, lianas);
        }
        double[] fork = trunkCenter(SPLIT_HEIGHT);
        for (int k = 0; k < 5; k++) {
            double az = 2 * Math.PI * k / 5 + 0.5 + rng.nextDouble(-0.25, 0.25);
            branch(t, new double[]{fork[0] + Math.cos(az) * 1.5, SPLIT_HEIGHT, fork[1] + Math.sin(az) * 1.5}, az, Math.toRadians(rng.nextDouble(42, 54)), rng.nextDouble(30, 34), 3.2, 0, clumps, lianas);
        }
        branch(t, new double[]{fork[0], SPLIT_HEIGHT, fork[1]}, 0.3, Math.toRadians(82), 36, 2.8, 0, clumps, lianas);   // fleche centrale
        // Houppier : grappes irregulieres (coquille : l'interieur ne se voit pas)
        for (double[] cl : clumps) {
            double r = cl[3], ry = r * 0.62;
            int R = (int) Math.ceil(r);
            for (int dx = -R; dx <= R; dx++) for (int dy = -R; dy <= R; dy++) for (int dz = -R; dz <= R; dz++) {
                int x = (int) Math.round(cl[0]) + dx, y = (int) Math.round(cl[1]) + dy, z = (int) Math.round(cl[2]) + dz;
                double e = (dx * dx + dz * dz) / (r * r) + (dy + 0.8) * (dy + 0.8) / (ry * ry) + 0.35 * noise(x >> 1, y >> 1, z >> 1);
                if (e > 1 || e < 0.45 || rng.nextDouble() >= 0.8) continue;
                t.put(x, y, z, rng.nextDouble() < 0.15 ? GOLD_LEAVES : LEAVES, false);
            }
        }
        // Lianes sous les branches
        for (double[] l : lianas) {
            double x = Math.round(l[0] + rng.nextDouble(-1, 1)), z = Math.round(l[2] + rng.nextDouble(-1, 1)), y = Math.round(l[1] - l[3] - 0.5);
            int n = 4 + rng.nextInt(9);
            for (int i = 0; i < n; i++) {
                if (t.has(x, y - i - 1, z) || i == n - 1) { if (i > 0) t.put(x, y - i, z, LIANA_END, false); break; }
                if (t.has(x, y - i, z)) break;
                t.put(x, y - i, z, LIANA, false);
            }
        }
        g.tree.addAll(t.cells.values());
        g.tree.sort(Comparator.comparingInt(Cell::y));
    }

    /** Branche courbe (part plutot a plat puis se redresse), ramifiee jusqu'a la profondeur 2, grappe au bout. */
    private static void branch(TreePlan t, double[] p0, double az, double el, double length, double r0, int depth, List<double[]> clumps, List<double[]> lianas) {
        Random rng = t.rng;
        double[] d = {Math.cos(az) * Math.cos(el), Math.sin(el), Math.sin(az) * Math.cos(el)};
        double[] mid = {p0[0] + d[0] * length * 0.5, p0[1] + d[1] * length * 0.5 - length * 0.04, p0[2] + d[2] * length * 0.5};
        double[] end = {p0[0] + d[0] * length, p0[1] + d[1] * length + length * (depth == 0 ? 0.10 : 0.04), p0[2] + d[2] * length};
        double r1 = Math.max(0.6, r0 * 0.45);
        int n = depth == 0 ? 6 : 4;
        double[] prev = p0;
        for (int k = 1; k <= n; k++) {
            double[] q = bezier(p0, mid, end, k / (double) n);
            capsule(t, TRUNK, prev, q, r0 + (r1 - r0) * (k - 1) / n, r0 + (r1 - r0) * k / n, true);
            if (depth < 2 && k < n && rng.nextDouble() < (depth == 0 ? 0.6 : 0.35)) lianas.add(new double[]{q[0], q[1], q[2], r0 * 0.6});
            prev = q;
        }
        if (depth < 2) {
            double[][] forks = {{0.4, 1}, {0.65, 1}, {0.85, 2}};
            for (double[] f : forks) {
                for (int i = 0; i < f[1]; i++) {
                    double[] q = bezier(p0, mid, end, f[0]);
                    double caz = az + (rng.nextBoolean() ? 1 : -1) * rng.nextDouble(0.45, 1.05);
                    double cel = Math.max(-0.1, Math.min(0.9, el * 0.7 + rng.nextDouble(-0.15, 0.3)));
                    branch(t, q, caz, cel, length * rng.nextDouble(0.4, 0.52), Math.max(0.7, r0 * (0.55 - 0.1 * f[0])), depth + 1, clumps, lianas);
                }
            }
        }
        clumps.add(new double[]{end[0], end[1], end[2], (depth == 2 ? 5.0 : 6.0) * rng.nextDouble(0.85, 1.15)});
    }

    private static double[] bezier(double[] a, double[] b, double[] c, double t) {
        double u = 1 - t;
        return new double[]{u * u * a[0] + 2 * u * t * b[0] + t * t * c[0], u * u * a[1] + 2 * u * t * b[1] + t * t * c[1], u * u * a[2] + 2 * u * t * b[2] + t * t * c[2]};
    }

    private static double noise(int x, int y, int z) {
        int h = (x * 73856093) ^ (y * 19349663) ^ (z * 83492791);
        return (h & 0xFFFF) / 65535.0 - 0.5;
    }

    /** Cylindre a bouts arrondis de a vers b (rayon ra -> rb), en coordonnees relatives. */
    private static void capsule(TreePlan t, String block, double[] a, double[] b, double ra, double rb, boolean override) {
        double rm = Math.max(ra, rb);
        double vx = b[0] - a[0], vy = b[1] - a[1], vz = b[2] - a[2], l2 = vx * vx + vy * vy + vz * vz;
        for (int x = (int) Math.floor(Math.min(a[0], b[0]) - rm); x <= Math.ceil(Math.max(a[0], b[0]) + rm); x++)
            for (int y = (int) Math.floor(Math.min(a[1], b[1]) - rm); y <= Math.ceil(Math.max(a[1], b[1]) + rm); y++)
                for (int z = (int) Math.floor(Math.min(a[2], b[2]) - rm); z <= Math.ceil(Math.max(a[2], b[2]) + rm); z++) {
                    double s = l2 < 1e-9 ? 0 : Math.max(0, Math.min(1, ((x - a[0]) * vx + (y - a[1]) * vy + (z - a[2]) * vz) / l2));
                    double px = a[0] + vx * s - x, py = a[1] + vy * s - y, pz = a[2] + vz * s - z, r = ra + (rb - ra) * s;
                    if (px * px + py * py + pz * pz <= r * r) t.put(x, y, z, block, override);
                }
    }

    private static long key(int x, int y, int z) {
        return (((long) x & 0x1FFFFF) << 42) | (((long) y & 0x1FFFFF) << 21) | ((long) z & 0x1FFFFF);
    }

    private static int[] unkey(long k) {
        int x = (int) (k >> 42), y = (int) ((k >> 21) & 0x1FFFFF), z = (int) (k & 0x1FFFFF);
        if (x >= 1 << 20) x -= 1 << 21;
        if (y >= 1 << 20) y -= 1 << 21;
        if (z >= 1 << 20) z -= 1 << 21;
        return new int[]{x, y, z};
    }

    /**
     * Salle de la fontaine sous le tronc (coupole de {@link #HALL_RADIUS} blocs) et galeries qui y descendent en
     * pente douce sous les grandes racines, depuis le jardin. Les vides sont creuses, puis tapisses d'une coquille de
     * bois et de mousse (une case d'epaisseur) qui sort de terre en tunnel de racines pres de l'entree.
     */
    private static void planBurrow(Grove g, World world) {
        int cx = (int) Math.floor(g.center.x), cy = (int) Math.floor(g.center.y), cz = (int) Math.floor(g.center.z);
        int ground = cy - 1, floor = ground - HALL_DEPTH;
        g.hallFloor = floor;
        Set<Long> air = new HashSet<>();
        int R = (int) Math.ceil(HALL_RADIUS);
        for (int dx = -R; dx <= R; dx++) for (int dz = -R; dz <= R; dz++) {
            double d = Math.hypot(dx, dz);
            for (int h = 1; h <= HALL_HEIGHT + 1; h++) {
                double v = (h - 1) / HALL_HEIGHT;
                if (v < 1 && d <= HALL_RADIUS * Math.sqrt(1 - v * v)) air.add(key(cx + dx, floor + h, cz + dz));
            }
        }
        for (int k : GALLERY_ROOTS) {
            if (k >= g.rootAngles.size()) continue;
            double a0 = g.rootAngles.get(k), r0 = HALL_RADIUS - 1.5;
            int mx = cx + (int) Math.round(Math.cos(a0) * GALLERY_MOUTH), mz = cz + (int) Math.round(Math.sin(a0) * GALLERY_MOUTH);
            int mouth = surface(world, mx, cy, mz);
            mouth = mouth == Integer.MIN_VALUE ? ground : Math.max(ground - 3, Math.min(ground + 3, mouth));
            int steps = (int) Math.ceil((GALLERY_MOUTH - r0) * 4);
            for (int i = 0; i <= steps; i++) {
                double t = i / (double) steps, r = r0 + (GALLERY_MOUTH - r0) * t, a = a0 + 0.25 * Math.sin(Math.PI * t);
                double px = Math.cos(a) * r, pz = Math.sin(a) * r;
                int f = floor + (int) Math.round((mouth - floor) * t);
                int W = (int) Math.ceil(GALLERY_HALF_WIDTH);
                for (int dx = -W - 1; dx <= W + 1; dx++) for (int dz = -W - 1; dz <= W + 1; dz++) {
                    int x = (int) Math.floor(px) + dx, z = (int) Math.floor(pz) + dz;
                    if (Math.hypot(x + 0.5 - px, z + 0.5 - pz) > GALLERY_HALF_WIDTH) continue;
                    for (int h = 1; h <= GALLERY_HEADROOM; h++) air.add(key(cx + x, f + h, cz + z));
                }
            }
        }
        // Coquille : toute case voisine d'un vide, sauf au-dela de l'entree des galeries (qui reste ouverte)
        for (long k : air) g.burrow.put(k, TerrainRestore.EMPTY);
        Random rng = new Random(cx * 31L + cz);
        for (long k : air) {
            int[] c = unkey(k);
            for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
                int x = c[0] + dx, y = c[1] + dy, z = c[2] + dz;
                long n = key(x, y, z);
                if (air.contains(n) || g.burrow.containsKey(n) || Math.hypot(x + 0.5 - g.center.x, z + 0.5 - g.center.z) > GALLERY_MOUTH - 0.3) continue;
                boolean floorCell = air.contains(key(x, y + 1, z));
                double d = Math.hypot(x - cx, z - cz);
                String block = floorCell && y == floor ? (d > HALL_RADIUS - 1.5 ? MOSS : (int) Math.floor(d) % 2 == 0 ? MARBLE : MARBLE_ALT)
                        : floorCell ? (rng.nextDouble() < 0.6 ? MOSS : TRUNK) : rng.nextDouble() < 0.15 ? MOSS : TRUNK;
                g.burrow.put(n, block);
            }
        }
        for (long k : g.burrow.keySet()) {
            int[] c = unkey(k);
            if (c[1] >= ground - 1) g.burrowColumns.add(key(c[0], 0, c[2]));
        }
        g.tree.removeIf(c -> TerrainRestore.EMPTY.equals(g.burrow.get(key(c.x(), c.y(), c.z()))));
    }

    /** Foret de l'anneau exterieur : positions tirees au hasard avec un ecart minimal (triees du centre vers le bord). */
    private static void planForest(Grove g) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        List<double[]> pts = new ArrayList<>();
        for (int tries = 0; tries < 900; tries++) {
            double a = rng.nextDouble(Math.PI * 2), d = Math.sqrt(rng.nextDouble(Math.pow(TRAP_RADIUS + 1.5, 2), Math.pow(RADIUS - 1, 2)));
            double x = Math.cos(a) * d, z = Math.sin(a) * d;
            boolean free = true;
            for (double[] p : pts) if (Math.hypot(p[0] - x, p[1] - z) < FOREST_SPACING) { free = false; break; }
            if (!free) continue;
            pts.add(new double[]{x, z});
            g.forest.add(new Sapling(a, d, rng.nextInt(FOREST_TREES.length), rng.nextLong()));
        }
        g.forest.sort(Comparator.comparingDouble(Sapling::dist));
    }

    /** Pieges vegetaux repartis dans l'anneau du milieu. */
    private static void planTraps(Grove g, World world) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < TRAP_COUNT; i++) {
            double a = Math.PI * 2 * i / TRAP_COUNT + rng.nextDouble(-0.15, 0.15);
            double d = GARDEN_RADIUS + 2 + rng.nextDouble(TRAP_RADIUS - GARDEN_RADIUS - 4);
            g.traps.add(new Trap(SpellFx.ground(world, new Vector3d(g.center).add(Math.cos(a) * d, 0, Math.sin(a) * d)).add(0, 1, 0)));
        }
    }

    // ===================== TICK =====================

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Grove> list = groves.get(uuid);
        if (list == null) return;
        long now = System.currentTimeMillis();
        for (Grove g : list) if (tickGrove(caster, g, now, store, cb)) list.remove(g);
        if (list.isEmpty()) groves.remove(uuid);
        if (!blessedUntil.isEmpty()) blessedUntil.entrySet().removeIf(e -> now >= e.getValue() || !e.getKey().isValid());
    }

    private static boolean tickGrove(Ref<EntityStore> caster, Grove g, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = g.world;
        long t = now - g.startAt;
        if (!g.opened) {
            g.opened = true;
            g.restore = TerrainRestore.open(world, g.startAt + RESTORE_AFTER_MS);
            TerrainRestore.Restore r = g.restore;
            world.execute(() -> {
                try { buildSanctuary(g, r); }
                catch (Exception e) { EldaniorLogger.error("YggdrasilAwakening: sanctuaire", e); }
            });
            SpellFx.fx(FX_SEED, new Vector3d(g.center).add(0, 1, 0), 4.0f, store, cb);
            SpellFx.fx(FX_WAVE, new Vector3d(g.center).add(0, 0.5, 0), 4.0f, store, cb);
            SpellFx.sound(SFX_GROW, g.center, cb);
            SpellFx.shake(g.center, RADIUS * 2, "Impact_Light", store);
        }
        if (t >= SWEEP_AFTER_MS) {                                                          // l'arbre a disparu : nettoyage final
            if (!g.swept) {
                g.swept = true;
                world.execute(() -> {
                    try { sweep(g); }
                    catch (Exception e) { EldaniorLogger.error("YggdrasilAwakening: nettoyage", e); }
                });
            }
            return true;
        }
        if (t >= DURATION_MS) {
            if (!g.finished) { g.finished = true; finale(caster, g, store, cb); TerrainRestore.save(); }
            return false;                                                                    // attend la restauration puis le nettoyage
        }
        spread(g, t, store, cb);
        grow(g, t, now, store, cb);
        if (t < EFFECTS_FROM_MS) return false;
        blessAndPoison(caster, g, now, store, cb);
        tickTraps(caster, g, now, store, cb);
        tickWave(caster, g, now, store, cb);
        if (now - g.lastMist >= MIST_EVERY_MS) {                                             // brume rasante sur l'anneau de la foret
            g.lastMist = now;
            int steps = (int) Math.ceil(RADIUS / MIST_SPACING);
            for (int i = -steps; i <= steps; i++) for (int j = -steps; j <= steps; j++) {
                double dx = i * MIST_SPACING, dz = j * MIST_SPACING, d = Math.hypot(dx, dz);
                if (d > RADIUS || d < TRAP_RADIUS) continue;
                SpellFx.fx(FX_MIST, SpellFx.ground(world, new Vector3d(g.center).add(dx, 0, dz)).add(0, 1.1, 0), 1.0f, store, cb);
            }
        }
        if (now - g.lastFountainFx >= FOUNTAIN_FX_EVERY_MS) {                               // fontaines de vie
            g.lastFountainFx = now;
            for (Vector3d f : g.fountains) SpellFx.fx(FX_FOUNTAIN, f, 1.0f, store, cb);
            if (!g.fountains.isEmpty() && (now / FOUNTAIN_FX_EVERY_MS) % 3 == 0) SpellFx.sound(SFX_FOUNTAIN, g.fountains.get(0), cb);
        }
        if (now - g.lastLeaves >= 450) {                                                     // feuilles d'or qui tombent de la canopee
            g.lastLeaves = now;
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            double a = rng.nextDouble(Math.PI * 2), r = rng.nextDouble(6, 30);
            SpellFx.fx(FX_LEAVES, new Vector3d(g.center).add(Math.cos(a) * r, rng.nextDouble(35, 85), Math.sin(a) * r), 3.0f, store, cb);
        }
        return false;
    }

    /** Onde de nature : transforme le sol anneau par anneau et plante la foret quand elle l'atteint. */
    private static void spread(Grove g, long t, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        double radius = Math.min(RADIUS, SPREAD_SPEED * t / 1000.0);
        if (radius <= g.spreadRadius) return;
        final double inner = g.spreadRadius, outer = radius;
        g.spreadRadius = radius;
        List<Sapling> saplings = new ArrayList<>();
        while (g.planted < g.forest.size() && g.forest.get(g.planted).dist() <= outer) saplings.add(g.forest.get(g.planted++));
        TerrainRestore.Restore r = g.restore;
        final boolean last = radius >= RADIUS;
        g.world.execute(() -> {
            try {
                transformRing(g, r, inner, outer);
                for (Sapling s : saplings) plantForestTree(g, r, s);
                if (last) TerrainRestore.save();
            } catch (Exception e) {
                EldaniorLogger.error("YggdrasilAwakening: onde de nature", e);
            }
        });
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < 5; i++) {
            double a = rng.nextDouble(Math.PI * 2);
            SpellFx.fx(i % 2 == 0 ? FX_LEAVES : FX_SPORES, new Vector3d(g.center).add(Math.cos(a) * radius, 0.8, Math.sin(a) * radius), 1.5f, store, cb);
        }
        if (rng.nextInt(4) == 0) SpellFx.sound(SFX_ROOT, new Vector3d(g.center).add(radius, 0, 0), cb);
    }

    /** Croissance de l'arbre-monde, couche par couche, du pied vers la cime. */
    private static void grow(Grove g, long t, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        int target = (int) Math.min(g.tree.size(), g.tree.size() * (t / (double) GROW_MS));
        if (target <= g.placed) return;
        List<Cell> batch = new ArrayList<>(g.tree.subList(g.placed, target));
        g.placed = target;
        TerrainRestore.Restore r = g.restore;
        g.world.execute(() -> {
            try { for (Cell c : batch) growCell(g, r, c); }
            catch (Exception e) { EldaniorLogger.error("YggdrasilAwakening: croissance", e); }
        });
        if ((now / 50) % 5 == 0) {
            Cell c = batch.get(batch.size() - 1);
            SpellFx.fx(FX_LEAVES, new Vector3d(c.x() + 0.5, c.y() + 0.5, c.z() + 0.5), 3.0f, store, cb);
        }
    }

    /** Jardin : benediction (fontaines : soin renforce et purification) ; foret : brume toxique ; milieu : ronces. */
    private static void blessAndPoison(Ref<EntityStore> caster, Grove g, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (now - g.lastPulse < 1_000) return;
        g.lastPulse = now;
        for (Ref<EntityStore> ally : SpellFx.alliesAround(caster, g.center, GARDEN_RADIUS + 20, store)) {
            Vector3d p = SpellFx.position(ally, store);
            if (p == null || SpellFx.horizontalDistance(p, g.center) > GARDEN_RADIUS) continue;
            SpellFx.effect(ally, BLESS_EFFECT, cb);
            SpellFx.restore(ally, DefaultEntityStatTypes.getHealth(), BLESS_HEAL, cb);
            SpellFx.restore(ally, DefaultEntityStatTypes.getStamina(), BLESS_STAMINA, cb);
            SpellFx.restore(ally, DefaultEntityStatTypes.getMana(), BLESS_MANA, cb);
            blessedUntil.put(ally, now + 1_600);
            for (Vector3d f : g.fountains) {                                                // dans la salle de la fontaine
                if (SpellFx.horizontalDistance(p, f) > HALL_RADIUS || p.y < g.hallFloor || p.y > g.hallFloor + HALL_HEIGHT + 1) continue;
                SpellFx.restore(ally, DefaultEntityStatTypes.getHealth(), FOUNTAIN_HEAL, cb);
                SpellFx.restore(ally, DefaultEntityStatTypes.getMana(), FOUNTAIN_MANA, cb);
                try { EffectsManager.cureAllDebuffs(ally, cb); } catch (Exception ignored) {}
                break;
            }
        }
        boolean poison = now - g.lastPoison >= POISON_EVERY_MS;
        if (poison) g.lastPoison = now;
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, g.center, RADIUS + 20, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            double d = SpellFx.horizontalDistance(p, g.center);
            if (d > RADIUS) continue;
            if (poison && d >= TRAP_RADIUS) SpellFx.effect(e, POISON_EFFECT, cb);
            if (d > GARDEN_RADIUS && d < TRAP_RADIUS) SpellFx.effect(e, THORN_EFFECT, cb);
        }
    }

    /** Pieges vegetaux : un ennemi qui marche dessus est enserre par des racines, immobilise et empoisonne. */
    private static void tickTraps(Ref<EntityStore> caster, Grove g, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        boolean fx = now - g.lastTrapFx >= TRAP_FX_EVERY_MS;
        if (fx) g.lastTrapFx = now;
        for (Trap trap : g.traps) {
            if (now < trap.armedAt) continue;
            if (fx) SpellFx.fx(FX_TRAP, trap.pos, 1.0f, store, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, trap.pos, TRAP_TRIGGER + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, trap.pos) > TRAP_TRIGGER || Math.abs(p.y - trap.pos.y) > 2.5) continue;
                trap.armedAt = now + TRAP_REARM_MS;
                SpellFx.lockInPlace(e, TRAP_ROOT_MS, store);
                SpellFx.effect(e, ROOT_EFFECT, cb);
                SpellFx.effect(e, POISON_EFFECT, cb);
                SpellFx.damage(caster, e, "Physical", TRAP_DAMAGE, null, "Impact_Light", cb);
                SpellFx.fx(FX_LEAVES, new Vector3d(p).add(0, 0.5, 0), 3.0f, store, cb);
                SpellFx.fx(FX_SPORES, new Vector3d(p).add(0, 0.3, 0), 0.6f, store, cb);
                SpellFx.sound(SFX_ROOT, p, cb);
                TerrainRestore.Restore roots = TerrainRestore.open(g.world, now + TRAP_ROOT_MS);
                Vector3d at = new Vector3d(p);
                g.world.execute(() -> {
                    try { rootsAround(g.world, roots, at); }
                    catch (Exception ex) { EldaniorLogger.error("YggdrasilAwakening: racines", ex); }
                });
                break;
            }
        }
    }

    /** Vague de poison : un anneau toxique traverse l'anneau du milieu, du jardin vers la foret. */
    private static void tickWave(Ref<EntityStore> caster, Grove g, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (g.waveAt < 0 && now - g.lastWave >= WAVE_EVERY_MS) {
            g.waveAt = now;
            g.lastWave = now;
            g.waveHit.clear();
            SpellFx.sound(SFX_ROOT, g.center, cb);
        }
        if (g.waveAt < 0) return;
        double progress = (now - g.waveAt) / (double) WAVE_TRAVEL_MS;
        if (progress > 1) { g.waveAt = -1; return; }
        double r = GARDEN_RADIUS + (TRAP_RADIUS - GARDEN_RADIUS) * progress;
        if (now - g.lastWaveFx >= 120) {
            g.lastWaveFx = now;
            int n = (int) Math.max(10, r * 0.9);
            double off = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
            for (int i = 0; i < n; i++) {
                double a = off + Math.PI * 2 * i / n;
                SpellFx.fx(FX_WAVE, new Vector3d(g.center).add(Math.cos(a) * r, 0.4, Math.sin(a) * r), 1.0f, store, cb);
            }
        }
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, g.center, TRAP_RADIUS + 20, store)) {
            if (g.waveHit.contains(e)) continue;
            Vector3d p = SpellFx.position(e, store);
            if (p == null || Math.abs(SpellFx.horizontalDistance(p, g.center) - r) > WAVE_WIDTH) continue;
            g.waveHit.add(e);
            SpellFx.damage(caster, e, "Physical", WAVE_DAMAGE, null, null, cb);
            SpellFx.effect(e, POISON_EFFECT, cb);
        }
    }

    /** Onde finale (juste avant que l'arbre disparaisse) : l'arbre-monde projette tous les ennemis de la zone. */
    private static void finale(Ref<EntityStore> caster, Grove g, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx(FX_EXPLOSION, new Vector3d(g.center).add(0, 1, 0), 6.0f, store, cb);
        SpellFx.fx(FX_SEED, new Vector3d(g.center).add(0, 30, 0), 10.0f, store, cb);
        for (int k = 0; k < 16; k++) {
            double a = k * Math.PI / 8;
            for (double r : new double[]{GARDEN_RADIUS, TRAP_RADIUS, RADIUS - 2}) {
                SpellFx.fx(FX_WAVE, SpellFx.ground(g.world, new Vector3d(g.center).add(Math.cos(a) * r, 0, Math.sin(a) * r)).add(0, 1, 0), 2.5f, store, cb);
            }
        }
        SpellFx.sound(SFX_FINAL, g.center, cb);
        SpellFx.shake(g.center, RADIUS * 2, "Impact_Strong", store);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, g.center, RADIUS + 20, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || SpellFx.horizontalDistance(p, g.center) > RADIUS) continue;
            Vector3d push = new Vector3d(p.x - g.center.x, 0, p.z - g.center.z);
            if (push.lengthSquared() < 1e-4) push.set(1, 0, 0);
            push.normalize().mul(FINAL_PUSH).add(0, FINAL_LIFT, 0);
            SpellFx.damage(caster, e, "Physical", FINAL_DAMAGE, push, "Impact_Strong", cb);
        }
    }

    // ===================== TERRAIN (hors tick ECS) =====================

    /** Pose un bloc dans l'air libre ou a la place de vegetation basse (jamais dans l'eau d'une fontaine). */
    private static void growCell(Grove g, TerrainRestore.Restore r, Cell c) {
        World world = g.world;
        if (TerrainRestore.isProtectedArea(world.getName(), c.x(), c.y(), c.z())) return;
        BlockType type = WorldBlockUtil.getBlockType(world, c.x(), c.y(), c.z());
        if (type == null) return;
        String id = type.getId();
        boolean empty = TerrainRestore.isEmpty(world, c.x(), c.y(), c.z());
        if (!empty && !isLowPlant(id)) return;
        if (empty && WorldBlockUtil.getFluidIndex(world, c.x(), c.y(), c.z()) > 0) return;
        if (TerrainRestore.place(world, r, c.x(), c.y(), c.z(), empty ? TerrainRestore.EMPTY : id, c.block(), 0) && empty) {
            g.placedInAir.add(key(c.x(), c.y(), c.z()));
        }
    }

    private static boolean isLowPlant(String id) {
        return id != null && id.startsWith("Plant_") && !id.contains("Leaves");
    }

    /** Remplace la surface (quel que soit le bloc) ; false si la colonne est protegee, conservee ou couverte d'arbre. */
    private static boolean replaceSurface(Grove g, TerrainRestore.Restore r, int x, int y, int z, String block) {
        if (TerrainRestore.isProtectedArea(g.world.getName(), x, y, z)) return false;
        BlockType type = WorldBlockUtil.getBlockType(g.world, x, y, z);
        if (type == null) return false;
        String id = type.getId();
        if (id.contains("Leaves") || id.contains("Trunk") || TerrainRestore.isKept(g.world, id, x, y, z)) return false;
        if (!id.equals(block)) TerrainRestore.place(g.world, r, x, y, z, id, block, 0);
        return true;
    }

    /**
     * Sanctuaire sous l'arbre : salle de la fontaine et galeries sous le tronc, dallage de marbre en couronne autour
     * du tronc et huit colonnes surmontees de cristaux.
     */
    private static void buildSanctuary(Grove g, TerrainRestore.Restore r) {
        int cx = (int) Math.floor(g.center.x), cy = (int) Math.floor(g.center.y), cz = (int) Math.floor(g.center.z);
        buildBurrow(g, r);
        int R = (int) Math.ceil(PLAZA_OUTER);
        for (int dx = -R; dx <= R; dx++) for (int dz = -R; dz <= R; dz++) {
            double d = Math.hypot(dx, dz);
            if (d <= PLAZA_INNER || d > PLAZA_OUTER || g.burrowColumns.contains(key(cx + dx, 0, cz + dz))) continue;
            int x = cx + dx, z = cz + dz, y = surface(g.world, x, cy, z);
            if (y == Integer.MIN_VALUE) continue;
            int sector = (int) Math.floor((Math.atan2(dz, dx) + Math.PI) / (Math.PI / 8));
            String slab = d > PLAZA_OUTER - 1 ? MARBLE_BORDER : sector % 2 == 0 ? MARBLE : MARBLE_ALT;
            if (replaceSurface(g, r, x, y, z, slab)) clearAbove(g, r, x, y + 1, z);
        }
        for (int k = 0; k < COLUMNS; k++) {
            double a = Math.PI / 8 + k * Math.PI / 4;
            int x = cx + (int) Math.round(Math.cos(a) * COLUMN_RING), z = cz + (int) Math.round(Math.sin(a) * COLUMN_RING);
            if (g.burrowColumns.contains(key(x, 0, z))) continue;
            int base = surface(g.world, x, cy, z);
            if (base == Integer.MIN_VALUE || !replaceSurface(g, r, x, base, z, MARBLE_BORDER)) continue;
            setInAir(g, r, x, base + 1, z, PILLAR_BASE);
            for (int h = 2; h <= 4; h++) setInAir(g, r, x, base + h, z, PILLAR);
            setInAir(g, r, x, base + 5, z, CRYSTAL);
        }
        TerrainRestore.save();
    }

    /**
     * Creuse la salle et les galeries, les tapisse de bois et de mousse, puis les amenage : fontaine de vie au centre
     * (bassin de marbre, colonne, grand cristal), cristaux lumineux le long des parois, racines pendant de la voute.
     */
    private static void buildBurrow(Grove g, TerrainRestore.Restore r) {
        World world = g.world;
        int cx = (int) Math.floor(g.center.x), cz = (int) Math.floor(g.center.z), floor = g.hallFloor;
        for (int pass = 0; pass < 2; pass++) {                                              // vides d'abord, coquille ensuite
            for (Map.Entry<Long, String> e : g.burrow.entrySet()) {
                boolean hollow = TerrainRestore.EMPTY.equals(e.getValue());
                if (hollow != (pass == 0)) continue;
                int[] c = unkey(e.getKey());
                if (TerrainRestore.isProtectedArea(world.getName(), c[0], c[1], c[2])) continue;
                BlockType type = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                if (type == null || TerrainRestore.isKept(world, type.getId(), c[0], c[1], c[2])) continue;
                boolean empty = TerrainRestore.isEmpty(world, c[0], c[1], c[2]);
                if (hollow) { if (!empty) TerrainRestore.place(world, r, c[0], c[1], c[2], type.getId(), TerrainRestore.EMPTY, 0); }
                else if (TerrainRestore.place(world, r, c[0], c[1], c[2], empty ? TerrainRestore.EMPTY : type.getId(), e.getValue(), 0) && empty) {
                    g.placedInAir.add(e.getKey());
                }
            }
        }
        // Fontaine de vie
        int B = (int) Math.ceil(BASIN_RADIUS);
        for (int i = -B; i <= B; i++) for (int j = -B; j <= B; j++) {
            double d = Math.hypot(i, j);
            if (d > BASIN_RADIUS) continue;
            int x = cx + i, z = cz + j;
            if (d > BASIN_RADIUS - 1) setInAir(g, r, x, floor + 1, z, BASIN_RIM);
            else if (i == 0 && j == 0) {
                setInAir(g, r, x, floor + 1, z, PILLAR_BASE);
                for (int h = 2; h <= 3; h++) setInAir(g, r, x, floor + h, z, PILLAR);
                setInAir(g, r, x, floor + 4, z, BIG_CRYSTAL);
            } else if (TerrainRestore.isEmpty(world, x, floor + 1, z) && TerrainRestore.placeFluid(world, r, x, floor + 1, z, WATER)) {
                g.water.add(key(x, floor + 1, z));
            }
        }
        g.fountains.add(new Vector3d(cx + 0.5, floor + 5.5, cz + 0.5));
        // Cristaux lumineux au pied des parois, racines qui pendent de la voute
        Random rng = new Random(cx * 17L + cz);
        for (Map.Entry<Long, String> e : g.burrow.entrySet()) {
            if (!TerrainRestore.EMPTY.equals(e.getValue())) continue;
            int[] c = unkey(e.getKey());
            String below = g.burrow.get(key(c[0], c[1] - 1, c[2])), above = g.burrow.get(key(c[0], c[1] + 1, c[2]));
            double d = Math.hypot(c[0] - cx, c[2] - cz);
            if (d <= BASIN_RADIUS + 0.5) continue;
            boolean onFloor = below != null && !TerrainRestore.EMPTY.equals(below), underRoof = above != null && !TerrainRestore.EMPTY.equals(above);
            boolean byWall = false;
            for (int[] o : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                String side = g.burrow.get(key(c[0] + o[0], c[1], c[2] + o[1]));
                if (side != null && !TerrainRestore.EMPTY.equals(side)) byWall = true;
            }
            if (onFloor && byWall && rng.nextDouble() < 0.18) setInAir(g, r, c[0], c[1], c[2], CRYSTAL);
            else if (underRoof && !onFloor && rng.nextDouble() < 0.14) setInAir(g, r, c[0], c[1], c[2], ROOTS);
        }
    }

    /** Pose un bloc dans une case libre (ou a la place d'une plante basse). */
    private static void setInAir(Grove g, TerrainRestore.Restore r, int x, int y, int z, String block) {
        growCell(g, r, new Cell(x, y, z, block));
    }

    /** Retire une plante basse (fleur, herbe) posee sur le dallage. */
    private static void clearAbove(Grove g, TerrainRestore.Restore r, int x, int y, int z) {
        BlockType above = WorldBlockUtil.getBlockType(g.world, x, y, z);
        if (above != null && isLowPlant(above.getId())) TerrainRestore.place(g.world, r, x, y, z, above.getId(), TerrainRestore.EMPTY, 0);
    }

    /** Transforme le sol de l'anneau (inner, outer] : surface en herbe (quel que soit le bloc), vegetation par anneau. */
    private static void transformRing(Grove g, TerrainRestore.Restore r, double inner, double outer) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int R = (int) Math.ceil(outer), cx = (int) Math.floor(g.center.x), cy = (int) Math.floor(g.center.y), cz = (int) Math.floor(g.center.z);
        for (int dx = -R; dx <= R; dx++) for (int dz = -R; dz <= R; dz++) {
            double d = Math.hypot(dx, dz);
            if (d > outer || d <= inner || d <= PLAZA_OUTER + 0.5) continue;                 // le sanctuaire a son propre sol
            int tier = d <= GARDEN_RADIUS ? 0 : d <= TRAP_RADIUS ? 1 : 2;
            int x = cx + dx, z = cz + dz;
            if (g.burrowColumns.contains(key(x, 0, z))) continue;                            // galerie qui sort de terre
            int y = surface(g.world, x, cy, z);
            if (y == Integer.MIN_VALUE || !replaceSurface(g, r, x, y, z, GROUND[tier])) continue;
            if (isNearColumn(dx, dz)) continue;
            String plant = decoration(tier, rng);
            if (plant != null) growCell(g, r, new Cell(x, y + 1, z, plant));
        }
    }

    private static boolean isNearColumn(int dx, int dz) {
        for (int k = 0; k < COLUMNS; k++) {
            double a = Math.PI / 8 + k * Math.PI / 4;
            if (Math.hypot(dx - Math.cos(a) * COLUMN_RING, dz - Math.sin(a) * COLUMN_RING) < 1.2) return true;
        }
        return false;
    }

    private static String decoration(int tier, ThreadLocalRandom rng) {
        double roll = rng.nextDouble();
        return switch (tier) {
            case 0 -> roll < 0.45 ? FLOWERS[rng.nextInt(FLOWERS.length)] : roll < 0.6 ? "Plant_Grass_Lush_Short" : roll < 0.64 ? "Plant_Bush_Lush" : null;
            case 1 -> roll < 0.30 ? POISON_PLANTS[rng.nextInt(POISON_PLANTS.length)] : null;
            default -> roll < 0.40 ? FOREST_FLOOR[rng.nextInt(FOREST_FLOOR.length)] : null;
        };
    }

    /** Premier bloc solide (hors vegetation basse) en partant du haut, ou MIN_VALUE si rien / section non chargee. */
    private static int surface(World world, int x, int baseY, int z) {
        for (int y = baseY + SCAN_UP; y >= baseY - SCAN_DOWN; y--) {
            BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
            if (type == null) return Integer.MIN_VALUE;
            if (TerrainRestore.isEmpty(world, x, y, z) || isLowPlant(type.getId())) continue;
            return y;
        }
        return Integer.MIN_VALUE;
    }

    /** Arbre de la foret : chene, bouleau, hetre, erable (couronne ronde) ou sapin (cone). */
    private static void plantForestTree(Grove g, TerrainRestore.Restore r, Sapling s) {
        int x = (int) Math.floor(g.center.x + Math.cos(s.angle()) * s.dist()), z = (int) Math.floor(g.center.z + Math.sin(s.angle()) * s.dist());
        int y = surface(g.world, x, (int) Math.floor(g.center.y), z);
        if (y == Integer.MIN_VALUE || TerrainRestore.isProtectedArea(g.world.getName(), x, y, z)) return;
        BlockType ground = WorldBlockUtil.getBlockType(g.world, x, y, z);
        if (ground == null || ground.getId().contains("Leaves") || ground.getId().contains("Trunk")) return;
        Random rng = new Random(s.seed());
        String trunk = FOREST_TREES[s.kind()][0], leaves = FOREST_TREES[s.kind()][1];
        boolean fir = s.kind() == 4, birch = s.kind() == 1;
        int height = fir ? 10 + rng.nextInt(5) : birch ? 8 + rng.nextInt(4) : 6 + rng.nextInt(4);
        List<Cell> cells = new ArrayList<>();
        for (int h = 1; h <= height; h++) cells.add(new Cell(x, y + h, z, trunk));
        if (fir) {
            for (int h = 3; h <= height + 2; h++) {
                double rr = (height + 2 - h) / (double) (height - 1) * 3.2 + 0.4;
                int R = (int) Math.ceil(rr);
                for (int dx = -R; dx <= R; dx++) for (int dz = -R; dz <= R; dz++)
                    if ((dx != 0 || dz != 0 || h > height) && Math.hypot(dx, dz) <= rr && rng.nextDouble() < 0.9) cells.add(new Cell(x + dx, y + h, z + dz, leaves));
            }
        } else {
            double rx = birch ? 2.2 : 3.0 + rng.nextDouble(), ry = birch ? 3.2 : 2.6;
            int top = y + height;
            for (int dx = -4; dx <= 4; dx++) for (int dy = -4; dy <= 3; dy++) for (int dz = -4; dz <= 4; dz++) {
                double e = (dx * dx + dz * dz) / (rx * rx) + (dy * dy) / (ry * ry);
                if (e <= 1 && (dx != 0 || dz != 0 || dy > 0) && rng.nextDouble() < 0.95 - e * 0.3) cells.add(new Cell(x + dx, top + dy, z + dz, leaves));
            }
        }
        for (Cell c : cells) growCell(g, r, c);
    }

    /** Racines qui enserrent l'ennemi (cases libres autour de ses pieds), retirees au bout de quelques secondes. */
    private static void rootsAround(World world, TerrainRestore.Restore r, Vector3d p) {
        int x = (int) Math.floor(p.x), y = (int) Math.floor(p.y), z = (int) Math.floor(p.z);
        if (TerrainRestore.isProtectedArea(world.getName(), x, y, z)) return;
        for (int[] o : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            if (TerrainRestore.isEmpty(world, x + o[0], y, z + o[1])) TerrainRestore.place(world, r, x + o[0], y, z + o[1], TerrainRestore.EMPTY, ROOTS, 0);
            if (TerrainRestore.isEmpty(world, x + o[0], y + 1, z + o[1])) TerrainRestore.place(world, r, x + o[0], y + 1, z + o[1], TerrainRestore.EMPTY, TRAP_BLOCK, 0);
        }
    }

    /**
     * Passage final, apres la restauration : toute case ou le sort avait pose un bloc dans l'air et qui contient encore
     * un bloc du sort (liane, feuille, fleur...) est videe ; l'eau de la fontaine est retiree.
     */
    private static void sweep(Grove g) {
        int removed = 0;
        for (long k : g.placedInAir) {
            int[] c = unkey(k);
            int x = c[0], y = c[1], z = c[2];
            BlockType type = WorldBlockUtil.getBlockType(g.world, x, y, z);
            if (type == null || type == BlockType.EMPTY || !TerrainRestore.contains(type.getId(), SPELL_BLOCKS)) continue;
            WorldBlockUtil.setBlock(g.world, x, y, z, TerrainRestore.EMPTY);
            removed++;
        }
        for (long k : g.water) {
            int[] c = unkey(k);
            if (WorldBlockUtil.getFluidIndex(g.world, c[0], c[1], c[2]) > 0) WorldBlockUtil.setFluid(g.world, c[0], c[1], c[2], null);
        }
        if (removed > 0) EldaniorLogger.info("[Yggdrasil] nettoyage final : " + removed + " bloc(s) residuel(s) retire(s)");
    }

    public static void handleDisconnect(UUID uuid) {
        if (groves.remove(uuid) != null) TerrainRestore.save();
    }
}
