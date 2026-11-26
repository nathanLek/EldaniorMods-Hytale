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
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Rune du Pilier du Monde (Terre, Unique, rune de predilection du Pilier des Mondes) : au point vise (25 blocs), un pilier
 * de basalte (rayon 2, 13 colonnes) jaillit sur 18 blocs (une couche / 80 ms, anneaux de marbre tous les 6 blocs). Les
 * ennemis a son emplacement sont projetes (250). Une fois dresse, il libere 3 ondes sismiques (toutes les 3 s) qui
 * parcourent le sol jusqu'a 20 blocs a 15 b/s : 120 degats + Gaia_Etourdi 1 s aux ennemis au sol. A 12 s il s'effondre
 * (150 autour de la base) et le terrain est restaure (jamais en ville ni sur une parcelle).
 * Fiche de conception : scripts/runes/specs/Rune_PilierDuMonde.json
 */
public final class PilierDuMonde implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 12_000, LAYER_MS = 80, WAVE_EVERY_MS = 3_000, WAVE_FX_EVERY_MS = 150, STUN_MS = 1_000;
    private static final double RANGE = 25.0, PILLAR_RADIUS = 2.2, LAUNCH_RADIUS = 2.5, WAVE_START = 3.0, WAVE_END = 20.0, WAVE_SPEED = 15.0;
    private static final double WAVE_WIDTH = 2.0, GROUND_TOLERANCE = 3.0, COLLAPSE_RADIUS = 5.0, LAUNCH_UP = 12.0;
    private static final int LAYERS = 18, RING_EVERY = 6, WAVES = 3, WAVE_POINTS = 24;
    private static final float LAUNCH = 250f, WAVE = 120f, COLLAPSE = 150f;

    // ===== Assets =====
    private static final String BODY = "Rock_Basalt", RING = "Rock_Marble_Brick_Ornate", STUN = "Gaia_Etourdi";
    private static final String FX_DUST = "Gaia_Dust_Burst", FX_CRUMBS = "Block_Break_Dust";
    private static final String SFX_RISE = "SFX_Golem_Earth_Wake", SFX_WAVE = "SFX_Golem_Earth_Stomp_Impact", SFX_COLLAPSE = "SFX_Golem_Earth_Slam_Impact";

    @Override public String id() { return "PilierDuMonde"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        if (SpellFx.position(caster, store) == null || world == null) return null;
        Vector3d aim = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        int bx = (int) Math.floor(aim.x), bz = (int) Math.floor(aim.z), by = floor(world, bx, (int) Math.floor(aim.y), bz);
        Vector3d base = new Vector3d(bx + 0.5, by, bz + 0.5);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, base, LAUNCH_RADIUS + GROUND_TOLERANCE, store)) {   // projetes par la sortie
            Vector3d p = SpellFx.position(e, store);
            if (p == null || SpellFx.horizontalDistance(p, base) > LAUNCH_RADIUS || Math.abs(p.y - by) > GROUND_TOLERANCE) continue;
            SpellFx.damage(caster, e, "Bludgeoning", LAUNCH, new Vector3d(0, LAUNCH_UP, 0), "Impact_Strong", cb);
        }
        List<int[]> columns = new ArrayList<>();
        int r = (int) Math.ceil(PILLAR_RADIUS);
        for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++)
            if (Math.hypot(dx, dz) <= PILLAR_RADIUS) columns.add(new int[]{bx + dx, bz + dz});
        long now = System.currentTimeMillis();
        SpellFx.fx(FX_DUST, base, 3.0f, store, cb);
        SpellFx.sound(SFX_RISE, base, cb);
        SpellFx.shake(base, 25, "Impact_Strong", store);
        return new Pillar(world, base, by, columns, TerrainRestore.open(world, now + DURATION_MS), now);
    }

    /** Premiere case libre au-dessus du sol local (de 3 au-dessus a 4 en dessous), sinon la hauteur visee. */
    private static int floor(World world, int x, int y0, int z) {
        for (int y = y0 + 3; y >= y0 - 4; y--) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
            if (t != null && t.getMaterial() == BlockMaterial.Solid && above != null && above.getMaterial() != BlockMaterial.Solid) return y + 1;
        }
        return y0;
    }

    /** Une onde sismique en cours. */
    private static final class Wave {
        final long startAt;
        final Set<Ref<EntityStore>> hit = new HashSet<>();
        long lastFx;
        Wave(long startAt) { this.startAt = startAt; }
        double radius(long now) { return WAVE_START + WAVE_SPEED * (now - startAt) / 1000.0; }
    }

    private static final class Pillar implements Instance {
        final World world;
        final Vector3d base;
        final int by;
        final List<int[]> columns;
        final TerrainRestore.Restore batch;
        final long startAt;
        final List<Wave> waves = new ArrayList<>();
        int raised;
        boolean blocked;
        long topAt, lastLayer;

        Pillar(World world, Vector3d base, int by, List<int[]> columns, TerrainRestore.Restore batch, long startAt) {
            this.world = world; this.base = base; this.by = by; this.columns = columns; this.batch = batch; this.startAt = startAt; this.lastLayer = startAt - LAYER_MS;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) { collapse(caster, store, cb, false); return true; }
            if (now - startAt >= DURATION_MS) { collapse(caster, store, cb, true); return true; }
            if (topAt == 0) {
                if (now - lastLayer >= LAYER_MS) {
                    lastLayer = now;
                    if (raised >= LAYERS || blocked) { topAt = now; SpellFx.shake(base, 25, "Impact_Strong", store); }
                    else raise(store, cb);
                }
                return false;
            }
            if (waves.size() < WAVES && now - topAt >= waves.size() * WAVE_EVERY_MS) {
                waves.add(new Wave(now));
                SpellFx.sound(SFX_WAVE, base, cb);
                SpellFx.shake(base, WAVE_END + 5, "Impact_Strong", store);
            }
            for (Wave w : waves) advance(caster, w, now, store, cb);
            return false;
        }

        /** Pose la couche suivante (le plafond arrete le pilier). */
        private void raise(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            int y = by + raised;
            BlockType center = WorldBlockUtil.getBlockType(world, (int) Math.floor(base.x), y, (int) Math.floor(base.z));
            if (center == null || center.getMaterial() == BlockMaterial.Solid && raised > 0) { blocked = true; return; }
            String id = raised % RING_EVERY == RING_EVERY - 1 ? RING : BODY;
            raised++;
            world.execute(() -> {
                try {
                    String w = world.getName();
                    for (int[] c : columns) {
                        if (TerrainRestore.isEmpty(world, c[0], y, c[1]) && !TerrainRestore.isProtectedArea(w, c[0], y, c[1])
                                && WorldBlockUtil.getFluidIndex(world, c[0], y, c[1]) <= 0)
                            TerrainRestore.place(world, batch, c[0], y, c[1], TerrainRestore.EMPTY, id, 0);
                    }
                    if (raised >= LAYERS) TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("PilierDuMonde: couche", e);
                }
            });
            if (raised % 3 == 0) SpellFx.fx(FX_DUST, new Vector3d(base).add(0, y - by, 0), 1.5f, store, cb);
        }

        /** L'onde s'elargit au sol et frappe une fois chaque ennemi qu'elle traverse. */
        private void advance(Ref<EntityStore> caster, Wave w, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            double r = w.radius(now);
            if (r > WAVE_END + WAVE_WIDTH) return;
            if (now - w.lastFx >= WAVE_FX_EVERY_MS && r <= WAVE_END) {
                w.lastFx = now;
                for (int i = 0; i < WAVE_POINTS; i++) {
                    double a = i * 2 * Math.PI / WAVE_POINTS + r * 0.1;
                    SpellFx.fx(FX_DUST, new Vector3d(base).add(Math.cos(a) * r, 0.2, Math.sin(a) * r), 0.9f, store, cb);
                }
            }
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, base, Math.min(r, WAVE_END) + GROUND_TOLERANCE, store)) {
                if (w.hit.contains(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                double d = SpellFx.horizontalDistance(p, base);
                if (d > r || d < r - WAVE_WIDTH || d > WAVE_END || Math.abs(p.y - by) > GROUND_TOLERANCE) continue;
                w.hit.add(e);
                SpellFx.damage(caster, e, "Earth", WAVE, null, "Impact_Light", cb);
                SpellFx.stun(e, STUN, STUN_MS, store, cb);
            }
        }

        /** Effondrement : le pilier disparait (restaure), gravats autour de la base. */
        private void collapse(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb, boolean blast) {
            world.execute(() -> { try { TerrainRestore.restoreNow(world, batch); TerrainRestore.save(); } catch (Exception e) { EldaniorLogger.error("PilierDuMonde: effondrement", e); } });
            for (int k = 0; k < raised; k += 3) SpellFx.fx(FX_CRUMBS, new Vector3d(base).add(0, k + 0.5, 0), 2.0f, store, cb);
            SpellFx.fx(FX_DUST, base, 3.5f, store, cb);
            SpellFx.sound(SFX_COLLAPSE, base, cb);
            if (!blast) return;
            SpellFx.shake(base, 25, "Impact_Strong", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, base, COLLAPSE_RADIUS + GROUND_TOLERANCE, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, base) > COLLAPSE_RADIUS || p.y < by - 2 || p.y > by + LAYERS) continue;
                SpellFx.damage(caster, e, "Bludgeoning", COLLAPSE, new Vector3d(0, 3, 0), "Impact_Strong", cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }

        @Override
        public void abort() { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }
    }
}
