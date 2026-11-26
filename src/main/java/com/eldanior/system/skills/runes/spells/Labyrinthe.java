package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune du Labyrinthe (Espace, Unique, rune de predilection de l'Architecte du Monde) : au point vise (25 blocs), trois
 * anneaux de Rock_Stone_Brick (r 4, 7, 10 ; 2 de haut ; deux breches de ~3 blocs par anneau) pendant 10 s, sans murer
 * les cases occupees. Toutes les 2 s l'espace se replie : chaque ennemi du labyrinthe (12 max) est teleporte sur une case
 * libre au hasard a plus de 3 blocs de sa place : 40 + Gaia_Etourdi 0,5 s au tick suivant. Terrain restaure.
 * Fiche de conception : scripts/runes/specs/Rune_Labyrinthe.json
 */
public final class Labyrinthe implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, LAYER_MS = 150, FOLD_EVERY_MS = 2_000, STUN_MS = 500;
    private static final double RANGE = 25.0, RADIUS = 10.0, INNER = 9.5, MIN_JUMP = 3.0, GAP = 1.5, HEIGHT = 4.0;
    private static final double[] RINGS = {4.0, 7.0, 10.0};
    private static final int WALL_HEIGHT = 2, MAX_TARGETS = 12, TRIES = 10;
    private static final float DAMAGE = 40f;

    // ===== Assets =====
    private static final String WALL = "Rock_Stone_Brick", STUN = "Gaia_Etourdi";
    private static final String FX_DUST = "Gaia_Dust_Burst", FX_FOLD = "Bifrost_Divine";
    private static final String SFX_OPEN = "SFX_Portal_Neutral_Open", SFX_FOLD = "SFX_Portal_Neutral";

    @Override public String id() { return "Labyrinthe"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        if (SpellFx.position(caster, store) == null || world == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        int cx = (int) Math.floor(center.x), cy = (int) Math.floor(center.y), cz = (int) Math.floor(center.z);
        Set<Long> occupied = new HashSet<>();                                     // pas de mur sur une creature
        for (Ref<EntityStore> e : TargetUtil.getAllEntitiesInSphere(center, RADIUS + 2, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p != null) occupied.add(key((int) Math.floor(p.x), (int) Math.floor(p.z)));
        }
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        List<int[]> cells = new ArrayList<>();                                     // x, sol local, z
        for (double r : RINGS) {
            double gap1 = rng.nextDouble(Math.PI * 2), gap2 = gap1 + Math.PI + rng.nextDouble(-0.6, 0.6), half = GAP / r;
            int ri = (int) Math.ceil(r) + 1;
            for (int dx = -ri; dx <= ri; dx++) for (int dz = -ri; dz <= ri; dz++) {
                if (Math.abs(Math.hypot(dx, dz) - r) > 0.5) continue;
                double a = Math.atan2(dz, dx);
                if (angleGap(a, gap1) < half || angleGap(a, gap2) < half) continue;
                int x = cx + dx, z = cz + dz;
                if (occupied.contains(key(x, z))) continue;
                cells.add(new int[]{x, floor(world, x, cy, z), z});
            }
        }
        long now = System.currentTimeMillis();
        SpellFx.fx(FX_FOLD, new Vector3d(center).add(0, 1, 0), 2.5f, store, cb);
        SpellFx.sound(SFX_OPEN, center, cb);
        return new Maze(world, center, cells, TerrainRestore.open(world, now + DURATION_MS + 2_000), now);
    }

    private static long key(int x, int z) { return ((long) x << 32) ^ (z & 0xffffffffL); }

    private static double angleGap(double a, double b) { return Math.abs(Math.IEEEremainder(a - b, Math.PI * 2)); }

    /** Premiere case libre au-dessus du sol local (de 3 au-dessus a 4 en dessous), sinon la hauteur visee. */
    private static int floor(World world, int x, int y0, int z) {
        for (int y = y0 + 3; y >= y0 - 4; y--) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
            if (t != null && t.getMaterial() == BlockMaterial.Solid && above != null && above.getMaterial() != BlockMaterial.Solid) return y + 1;
        }
        return y0;
    }

    private static final class Maze implements Instance {
        final World world;
        final Vector3d center;
        final List<int[]> cells;
        final TerrainRestore.Restore batch;
        final long startAt;
        final List<Ref<EntityStore>> stunNext = new ArrayList<>();
        int layers;
        long lastLayer, lastFold;

        Maze(World world, Vector3d center, List<int[]> cells, TerrainRestore.Restore batch, long startAt) {
            this.world = world; this.center = center; this.cells = cells; this.batch = batch; this.startAt = startAt;
            this.lastLayer = startAt - LAYER_MS; this.lastFold = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            for (Ref<EntityStore> e : stunNext) if (e.isValid()) SpellFx.stun(e, STUN, STUN_MS, store, cb);
            stunNext.clear();
            if (now - startAt >= DURATION_MS) { world.execute(() -> { try { TerrainRestore.restoreNow(world, batch); TerrainRestore.save(); } catch (Exception e) { EldaniorLogger.error("Labyrinthe: fin", e); } }); return true; }
            if (layers < WALL_HEIGHT && now - lastLayer >= LAYER_MS) { lastLayer = now; build(layers++, store, cb); }
            if (now - lastFold >= FOLD_EVERY_MS) { lastFold = now; fold(caster, store, cb); }
            return false;
        }

        private void build(int k, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            world.execute(() -> {
                try {
                    String w = world.getName();
                    for (int[] c : cells) {
                        int y = c[1] + k;
                        if (TerrainRestore.isEmpty(world, c[0], y, c[2]) && !TerrainRestore.isProtectedArea(w, c[0], y, c[2])
                                && WorldBlockUtil.getFluidIndex(world, c[0], y, c[2]) <= 0)
                            TerrainRestore.place(world, batch, c[0], y, c[2], TerrainRestore.EMPTY, WALL, 0);
                    }
                    TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("Labyrinthe: murs", e);
                }
            });
            for (int i = 0; i < cells.size(); i += 8) { int[] c = cells.get(i); SpellFx.fx(FX_DUST, new Vector3d(c[0] + 0.5, c[1] + k, c[2] + 0.5), 0.7f, store, cb); }
        }

        /** L'espace se replie : chaque ennemi est jete dans une autre allee. */
        private void fold(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            int n = 0;
            boolean any = false;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + HEIGHT, store)) {
                if (n >= MAX_TARGETS || RuneSpellRuntime.isSummon(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, center) > RADIUS + 0.5 || Math.abs(p.y - center.y) > HEIGHT) continue;
                n++;
                for (int i = 0; i < TRIES; i++) {
                    double a = rng.nextDouble(Math.PI * 2), r = INNER * Math.sqrt(rng.nextDouble());
                    Vector3d spot = LoiArcanique.feet(world, center.x + Math.cos(a) * r, center.y, center.z + Math.sin(a) * r);
                    if (spot == null || spot.distance(p) < MIN_JUMP || Math.abs(spot.y - center.y) > HEIGHT) continue;
                    BlockType below = WorldBlockUtil.getBlockType(world, (int) Math.floor(spot.x), (int) Math.floor(spot.y) - 1, (int) Math.floor(spot.z));
                    if (below != null && WALL.equals(below.getId())) continue;           // jamais au sommet d'un mur
                    SpellFx.fx(FX_FOLD, new Vector3d(p).add(0, 1, 0), 0.8f, store, cb);
                    SpellFx.fx(FX_FOLD, new Vector3d(spot).add(0, 1, 0), 0.8f, store, cb);
                    SpellFx.teleport(e, spot, store, cb);
                    SpellFx.damage(caster, e, "Elemental", DAMAGE, null, null, cb);
                    stunNext.add(e);
                    any = true;
                    break;
                }
            }
            if (any) SpellFx.sound(SFX_FOLD, center, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }

        @Override
        public void abort() { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }
    }
}
