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
 * Rune du Plan d'Architecte (Terre, Epic, rune de predilection de l'Architecte du Monde) : un trace au sol part de 2 blocs
 * devant le lanceur jusqu'au point vise (20 blocs). Apres 1 s de plan (Bifrost_Divine), un mur de Rock_Stone_Brick de 3 de
 * haut monte colonne par colonne (50 ms) depuis le lanceur ; les ennemis sur le trace sont ecartes sur le cote (60). Le mur
 * tient jusqu'a 11 s puis s'ecroule : 120 a 2 blocs. Terrain restaure.
 * Fiche de conception : scripts/runes/specs/Rune_PlanArchitecte.json
 */
public final class PlanArchitecte implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long PLAN_MS = 1_000, COLUMN_MS = 50, DURATION_MS = 11_000;
    private static final double RANGE = 20.0, START = 2.0, MIN_LENGTH = 4.0, SHOVE_RADIUS = 1.2, SHOVE = 8.0, COLLAPSE_RADIUS = 2.0;
    private static final int HEIGHT = 3;
    private static final float SHOVE_DAMAGE = 60f, COLLAPSE = 120f;

    // ===== Assets =====
    private static final String WALL = "Rock_Stone_Brick";
    private static final String FX_PLAN = "Bifrost_Divine", FX_DUST = "Gaia_Dust_Burst", FX_CRUMBS = "Block_Break_Dust";
    private static final String SFX_RISE = "SFX_Golem_Earth_Wake", SFX_FALL = "SFX_Golem_Earth_Slam_Impact";

    @Override public String id() { return "PlanArchitecte"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        Vector3d aim = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        Vector3d dir = new Vector3d(aim.x - pos.x, 0, aim.z - pos.z);
        double length = Math.max(MIN_LENGTH, Math.min(RANGE, dir.length()));
        if (dir.lengthSquared() < 1e-4) dir.set(SpellFx.forward(caster, store));
        dir.normalize();
        List<int[]> columns = new ArrayList<>();                                  // x, sol local, z
        int cy = (int) Math.floor(pos.y);
        for (double d = START; d <= length; d += 0.5) {
            int x = (int) Math.floor(pos.x + dir.x * d), z = (int) Math.floor(pos.z + dir.z * d);
            int[] last = columns.isEmpty() ? null : columns.get(columns.size() - 1);
            if (last != null && last[0] == x && last[2] == z) continue;
            int y = floor(world, x, last != null ? last[1] : cy, z);
            columns.add(new int[]{x, y, z});
            SpellFx.fx(FX_PLAN, new Vector3d(x + 0.5, y + 0.2, z + 0.5), 0.4f, store, cb);
        }
        long now = System.currentTimeMillis();
        return new Wall(world, dir, columns, TerrainRestore.open(world, now + DURATION_MS + 2_000), now);
    }

    /** Premiere case libre au-dessus du sol local (de 3 au-dessus a 4 en dessous), sinon la hauteur de depart. */
    private static int floor(World world, int x, int y0, int z) {
        for (int y = y0 + 3; y >= y0 - 4; y--) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
            if (t != null && t.getMaterial() == BlockMaterial.Solid && above != null && above.getMaterial() != BlockMaterial.Solid) return y + 1;
        }
        return y0;
    }

    private static final class Wall implements Instance {
        final World world;
        final Vector3d dir;
        final List<int[]> columns;
        final TerrainRestore.Restore batch;
        final long startAt;
        final Set<Ref<EntityStore>> shoved = new HashSet<>();
        int raised;
        long lastColumn;

        Wall(World world, Vector3d dir, List<int[]> columns, TerrainRestore.Restore batch, long startAt) {
            this.world = world; this.dir = dir; this.columns = columns; this.batch = batch; this.startAt = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            long t = now - startAt;
            if (t >= DURATION_MS) { collapse(caster, store, cb); return true; }
            if (t >= PLAN_MS && raised < columns.size() && now - lastColumn >= COLUMN_MS) {
                lastColumn = now;
                int[] c = columns.get(raised++);
                if (raised == 1) SpellFx.sound(SFX_RISE, new Vector3d(c[0], c[1], c[2]), cb);
                rise(caster, c, store, cb);
            }
            return false;
        }

        /** Une colonne du mur sort de terre ; les ennemis sur la case sont ecartes sur le cote. */
        private void rise(Ref<EntityStore> caster, int[] c, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d at = new Vector3d(c[0] + 0.5, c[1], c[2] + 0.5);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, SHOVE_RADIUS + HEIGHT, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || shoved.contains(e) || SpellFx.horizontalDistance(p, at) > SHOVE_RADIUS || Math.abs(p.y - at.y) > HEIGHT) continue;
                shoved.add(e);
                Vector3d side = new Vector3d(-dir.z, 0, dir.x);
                if (side.dot(new Vector3d(p).sub(at)) < 0) side.negate();             // du cote ou il se trouve deja
                SpellFx.damage(caster, e, "Bludgeoning", SHOVE_DAMAGE, side.mul(SHOVE).add(0, 3, 0), "Impact_Light", cb);
            }
            world.execute(() -> {
                try {
                    String w = world.getName();
                    for (int k = 0; k < HEIGHT; k++) {
                        int y = c[1] + k;
                        if (TerrainRestore.isEmpty(world, c[0], y, c[2]) && !TerrainRestore.isProtectedArea(w, c[0], y, c[2])
                                && WorldBlockUtil.getFluidIndex(world, c[0], y, c[2]) <= 0)
                            TerrainRestore.place(world, batch, c[0], y, c[2], TerrainRestore.EMPTY, WALL, 0);
                    }
                    TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("PlanArchitecte: colonne", e);
                }
            });
            SpellFx.fx(FX_DUST, at, 0.8f, store, cb);
        }

        /** Le mur s'ecroule sur ceux qui le longent. */
        private void collapse(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            world.execute(() -> { try { TerrainRestore.restoreNow(world, batch); TerrainRestore.save(); } catch (Exception e) { EldaniorLogger.error("PlanArchitecte: fin", e); } });
            Set<Ref<EntityStore>> hit = new HashSet<>();
            for (int i = 0; i < raised; i++) {
                int[] c = columns.get(i);
                Vector3d at = new Vector3d(c[0] + 0.5, c[1], c[2] + 0.5);
                if (i % 2 == 0) SpellFx.fx(FX_CRUMBS, new Vector3d(at).add(0, 1.5, 0), 1.2f, store, cb);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, COLLAPSE_RADIUS + HEIGHT, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || hit.contains(e) || SpellFx.horizontalDistance(p, at) > COLLAPSE_RADIUS) continue;
                    hit.add(e);
                    SpellFx.damage(caster, e, "Bludgeoning", COLLAPSE, null, "Impact_Strong", cb);
                }
            }
            if (!columns.isEmpty()) { int[] c = columns.get(columns.size() / 2); SpellFx.sound(SFX_FALL, new Vector3d(c[0], c[1], c[2]), cb); }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }

        @Override
        public void abort() { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }
    }
}
