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
import java.util.List;

/**
 * Rune du Rempart (Terre, Epic, rune de predilection du Thaumaturge Royal) : les ennemis a moins de 5,5 blocs sont
 * repousses dehors (30 degats), puis une muraille circulaire de Rock_Stone_Brick (rayon 5, 3 de haut, une couche toutes les
 * 200 ms) s'eleve autour du lanceur pendant 10 s. Chaque colonne part du sol local ; seules les cases vides recoivent un
 * bloc ; zones protegees intouchees. Tout est restaure a la fin.
 * Fiche de conception : scripts/runes/specs/Rune_Rempart.json
 */
public final class Rempart implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, LAYER_MS = 200;
    private static final int HEIGHT = 3;
    private static final double RADIUS = 5.0, CLEAR_RADIUS = 5.5, PUSH = 14.0, PUSH_LIFT = 4.0;
    private static final float DAMAGE = 30f;

    // ===== Assets =====
    private static final String WALL = "Rock_Stone_Brick";
    private static final String FX_DUST = "Gaia_Dust_Burst", FX_CRUMBS = "Block_Break_Dust";
    private static final String SFX_RISE = "SFX_Golem_Earth_Wake", SFX_FALL = "SFX_Golem_Earth_Slam_Impact";

    @Override public String id() { return "Rempart"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, CLEAR_RADIUS, store)) {   // personne n'est enferme avec vous
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            Vector3d away = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
            if (away.lengthSquared() < 1e-6) away.set(1, 0, 0);
            SpellFx.damage(caster, e, "Physical", DAMAGE, away.normalize().mul(PUSH).add(0, PUSH_LIFT, 0), "Impact_Light", cb);
        }
        int bx = (int) Math.floor(pos.x), by = (int) Math.floor(pos.y), bz = (int) Math.floor(pos.z), r = (int) Math.ceil(RADIUS) + 1;
        List<int[]> columns = new ArrayList<>();                               // x, sol (premiere case libre), z
        for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
            if (Math.abs(Math.hypot(dx, dz) - RADIUS) > 0.5) continue;
            int x = bx + dx, z = bz + dz;
            columns.add(new int[]{x, floor(world, x, by, z), z});
        }
        SpellFx.sound(SFX_RISE, pos, cb);
        long now = System.currentTimeMillis();
        return new Wall(world, new Vector3d(pos), columns, TerrainRestore.open(world, now + DURATION_MS), now);
    }

    /** Premiere case libre au-dessus du sol local (cherche de 2 au-dessus a 3 en dessous du lanceur), sinon la hauteur du lanceur. */
    private static int floor(World world, int x, int y0, int z) {
        for (int y = y0 + 2; y >= y0 - 3; y--) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
            if (t != null && t.getMaterial() == BlockMaterial.Solid && above != null && above.getMaterial() != BlockMaterial.Solid) return y + 1;
        }
        return y0;
    }

    private static final class Wall implements Instance {
        final World world;
        final Vector3d center;
        final List<int[]> columns;
        final TerrainRestore.Restore batch;
        final long startAt;
        int layers;
        long lastLayer;

        Wall(World world, Vector3d center, List<int[]> columns, TerrainRestore.Restore batch, long startAt) {
            this.world = world; this.center = center; this.columns = columns; this.batch = batch; this.startAt = startAt; this.lastLayer = startAt - LAYER_MS;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) { fall(store, cb); return true; }
            if (layers < HEIGHT && now - lastLayer >= LAYER_MS) {
                lastLayer = now;
                int k = layers++;
                world.execute(() -> {
                    try {
                        for (int[] c : columns) {
                            int y = c[1] + k;
                            if (TerrainRestore.isEmpty(world, c[0], y, c[2]) && !TerrainRestore.isProtectedArea(world.getName(), c[0], y, c[2])
                                    && WorldBlockUtil.getFluidIndex(world, c[0], y, c[2]) <= 0)
                                TerrainRestore.place(world, batch, c[0], y, c[2], TerrainRestore.EMPTY, WALL, 0);
                        }
                    } catch (Exception e) {
                        EldaniorLogger.error("Rempart: couche", e);
                    }
                });
                for (int i = 0; i < columns.size(); i += 4) {
                    int[] c = columns.get(i);
                    SpellFx.fx(FX_DUST, new Vector3d(c[0] + 0.5, c[1] + k, c[2] + 0.5), 0.8f, store, cb);
                }
            }
            return false;
        }

        private void fall(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            world.execute(() -> { try { TerrainRestore.restoreNow(world, batch); TerrainRestore.save(); } catch (Exception e) { EldaniorLogger.error("Rempart: fin", e); } });
            for (int i = 0; i < columns.size(); i += 3) {
                int[] c = columns.get(i);
                SpellFx.fx(FX_CRUMBS, new Vector3d(c[0] + 0.5, c[1] + 1, c[2] + 0.5), 1.0f, store, cb);
            }
            SpellFx.sound(SFX_FALL, center, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }

        @Override
        public void abort() { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }
    }
}
