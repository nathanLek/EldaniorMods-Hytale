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
 * Rune du Sculpteur de Glace (Glace, Unique, rune de predilection du Primordial Ancien) : 15 s pendant lesquelles chaque
 * clic sculpte la glace au point vise (20 blocs) : gauche = mur 4 x 3 en travers du regard, droit = rampe de 4 marches
 * (colonnes pleines de 1 a 4 blocs) vers l'avant, 4 de large ; axes ramenes aux directions cardinales. Six structures au
 * plus ; un 7e clic ou la fin du sort les fait toutes eclater : 100 + Elda_Givre aux ennemis a 3 blocs de chacune.
 * Fiche de conception : scripts/runes/specs/Rune_SculpteurDeGlace.json
 */
public final class SculpteurDeGlace implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 15_000, LAYER_MS = 100, CARVE_EVERY_MS = 400;
    private static final double RANGE = 20.0, SHATTER_RADIUS = 3.0;
    private static final int MAX_STRUCTURES = 6, WIDTH = 4, WALL_HEIGHT = 3, RAMP_STEPS = 4;
    private static final float SHATTER = 100f;

    // ===== Assets =====
    private static final String ICE = "Rock_Ice", FROST = "Elda_Givre";
    private static final String FX_GROW = "Block_Break_Ice", FX_SHATTER = "Ability_Ice_Explosion";
    private static final String SFX_GROW = "SFX_Ice_Build", SFX_SHATTER = "SFX_Ice_Break";

    @Override public String id() { return "SculpteurDeGlace"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null || world == null) return null;
        SpellFx.fx(FX_GROW, new Vector3d(pos).add(0, 1, 0), 1.0f, store, cb);
        long now = System.currentTimeMillis();
        return new Workshop(world, TerrainRestore.open(world, now + DURATION_MS + 2_000), now);
    }

    /** Une sculpture : ses couches (posees une par une) et son centre. */
    private static final class Sculpture {
        final List<List<int[]>> layers;
        final Vector3d center;
        int grown;
        long lastLayer;
        Sculpture(List<List<int[]>> layers, Vector3d center) { this.layers = layers; this.center = center; }
    }

    /** Premiere case libre au-dessus du sol local (de 3 au-dessus a 4 en dessous), sinon la hauteur visee. */
    private static int floor(World world, int x, int y0, int z) {
        for (int y = y0 + 3; y >= y0 - 4; y--) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
            if (t != null && t.getMaterial() == BlockMaterial.Solid && above != null && above.getMaterial() != BlockMaterial.Solid) return y + 1;
        }
        return y0;
    }

    private static final class Workshop implements Instance {
        final World world;
        final TerrainRestore.Restore batch;
        final long startAt;
        final List<Sculpture> sculptures = new ArrayList<>();
        volatile int request;                                                   // 0 rien, 1 mur, 2 rampe
        long lastCarve;
        boolean shattered;

        Workshop(World world, TerrainRestore.Restore batch, long startAt) { this.world = world; this.batch = batch; this.startAt = startAt; }

        @Override public boolean listensClicks() { return true; }

        @Override
        public void onClick(boolean left, Ref<EntityStore> target, Ref<EntityStore> casterRef, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            request = left ? 1 : 2;                                               // applique dans tick
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            if (now - startAt >= DURATION_MS) { shatter(caster, store, cb); return true; }
            int r = request;
            if (r != 0) {
                request = 0;
                if (now - lastCarve >= CARVE_EVERY_MS) {
                    lastCarve = now;
                    if (sculptures.size() >= MAX_STRUCTURES) { shatter(caster, store, cb); return true; }
                    carve(caster, r == 1, store, cb);
                }
            }
            for (Sculpture s : sculptures) if (s.grown < s.layers.size() && now - s.lastLayer >= LAYER_MS) { s.lastLayer = now; grow(s, store, cb); }
            return false;
        }

        /** Prepare un mur (en travers du regard) ou une rampe (vers l'avant) au point vise. */
        private void carve(Ref<EntityStore> caster, boolean wall, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d aim = SpellFx.targetPoint(caster, store, RANGE, RANGE);
            Vector3d fwd = SpellFx.forward(caster, store);
            int fx = 0, fz = 0;                                                   // direction cardinale du regard
            if (Math.abs(fwd.x) >= Math.abs(fwd.z)) fx = fwd.x >= 0 ? 1 : -1; else fz = fwd.z >= 0 ? 1 : -1;
            int sx = -fz, sz = fx;                                                // axe en travers
            int ax = (int) Math.floor(aim.x), az = (int) Math.floor(aim.z), base = floor(world, ax, (int) Math.floor(aim.y), az);
            List<List<int[]>> layers = new ArrayList<>();
            int height = wall ? WALL_HEIGHT : RAMP_STEPS;
            for (int k = 0; k < height; k++) layers.add(new ArrayList<>());
            for (int w = 0; w < WIDTH; w++) {
                int off = w - WIDTH / 2;
                if (wall) {
                    int x = ax + sx * off, z = az + sz * off, y0 = floor(world, x, base, z);
                    for (int k = 0; k < WALL_HEIGHT; k++) layers.get(k).add(new int[]{x, y0 + k, z});
                } else {
                    for (int step = 0; step < RAMP_STEPS; step++) {
                        int x = ax + sx * off + fx * step, z = az + sz * off + fz * step;
                        for (int k = 0; k <= step; k++) layers.get(k).add(new int[]{x, base + k, z});
                    }
                }
            }
            Vector3d center = new Vector3d(ax + 0.5 + (wall ? 0 : fx * 1.5), base + 1, az + 0.5 + (wall ? 0 : fz * 1.5));
            sculptures.add(new Sculpture(layers, center));
            SpellFx.sound(SFX_GROW, center, cb);
        }

        private void grow(Sculpture s, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            List<int[]> layer = s.layers.get(s.grown++);
            world.execute(() -> {
                try {
                    String w = world.getName();
                    for (int[] c : layer) {
                        if (TerrainRestore.isEmpty(world, c[0], c[1], c[2]) && !TerrainRestore.isProtectedArea(w, c[0], c[1], c[2])
                                && WorldBlockUtil.getFluidIndex(world, c[0], c[1], c[2]) <= 0)
                            TerrainRestore.place(world, batch, c[0], c[1], c[2], TerrainRestore.EMPTY, ICE, 0);
                    }
                    TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("SculpteurDeGlace: couche", e);
                }
            });
            if (!layer.isEmpty()) { int[] c = layer.get(layer.size() / 2); SpellFx.fx(FX_GROW, new Vector3d(c[0] + 0.5, c[1] + 0.5, c[2] + 0.5), 1.0f, store, cb); }
        }

        /** Toutes les sculptures volent en eclats. */
        private void shatter(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            shattered = true;
            world.execute(() -> { try { TerrainRestore.restoreNow(world, batch); TerrainRestore.save(); } catch (Exception e) { EldaniorLogger.error("SculpteurDeGlace: eclats", e); } });
            for (Sculpture s : sculptures) {
                SpellFx.fx(FX_SHATTER, s.center, 1.5f, store, cb);
                SpellFx.fx(FX_GROW, s.center, 1.5f, store, cb);
                SpellFx.sound(SFX_SHATTER, s.center, cb);
                Set<Ref<EntityStore>> hit = new HashSet<>();
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, s.center, SHATTER_RADIUS + 1, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || !hit.add(e) || p.distance(s.center) > SHATTER_RADIUS + 1) continue;
                    SpellFx.damage(caster, e, "Ice", SHATTER, null, "Impact_Light", cb);
                    SpellFx.effect(e, FROST, cb);
                }
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { if (!shattered) world.execute(() -> TerrainRestore.restoreNow(world, batch)); }

        @Override
        public void abort() { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }
    }
}
