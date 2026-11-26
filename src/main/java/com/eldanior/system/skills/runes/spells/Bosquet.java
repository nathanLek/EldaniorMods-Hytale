package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune du Bosquet (Terre, Epic, rune de predilection de l'Arche Druide) : au point vise (20 blocs), un bosquet de 10 s
 * (rayon 7) : 3 chenes qui poussent couche par couche au centre, une haie de ronces sur le pourtour (sur sol de terre).
 * Allies dedans (lanceur compris) : +1,5 % PV / 0,5 s ; ennemis : 12 degats / 0,5 s + Yggdrasil_Ronces (x0,6).
 * Tous les blocs sont restaures a la fin (jamais en ville ni sur une parcelle).
 * Fiche de conception : scripts/runes/specs/Rune_Bosquet.json
 */
public final class Bosquet implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, PULSE_EVERY_MS = 500, LAYER_MS = 150, CANOPY_FX_EVERY_MS = 1_000;
    private static final double RANGE = 20.0, RADIUS = 7.0, TREE_RING = 3.5, BRAMBLE_MIN = 5.5, BRAMBLE_CHANCE = 0.45;
    private static final float HEAL = 0.015f, DAMAGE = 12f;

    // ===== Assets =====
    private static final String TRUNK = "Wood_Oak_Trunk", LEAVES = "Plant_Leaves_Oak", BRAMBLE_BLOCK = "Plant_Bramble_Moss_Twisted";
    private static final String BRAMBLE = "Yggdrasil_Ronces";
    private static final String FX_LEAVES = "Block_Break_Leaves", FX_DIRT = "Block_Break_Dirt", FX_HEAL = "Apollo_Heal_Pulse", FX_DUST = "Gaia_Dust_Burst";
    private static final String SFX_GROW = "SFX_Deployable_Totem_Heal_Spawn", SFX_CREAK = "SFX_Emit_Tree_Creak", SFX_THORN = "SFX_Bramble_MoveIn";
    private static final String SFX_END = "SFX_Leaves_Break";

    @Override public String id() { return "Bosquet"; }

    /** Un bloc a poser (couche de pousse). */
    private record Cell(int x, int y, int z, String id) {}

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        if (SpellFx.position(caster, store) == null || world == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        int cx = (int) Math.floor(center.x), cy = (int) Math.floor(center.y), cz = (int) Math.floor(center.z);
        List<List<Cell>> layers = new ArrayList<>();                           // couche k : troncs puis houppiers
        for (int k = 0; k < 7; k++) layers.add(new ArrayList<>());
        List<Vector3d> crowns = new ArrayList<>();
        for (int i = 0; i < 3; i++) {                                          // trois chenes en triangle
            double a = Math.PI / 2 + i * 2 * Math.PI / 3;
            int x = cx + (int) Math.round(Math.cos(a) * TREE_RING), z = cz + (int) Math.round(Math.sin(a) * TREE_RING);
            int base = floor(world, x, cy, z);
            for (int k = 0; k < 5; k++) layers.get(k).add(new Cell(x, base + k, z, TRUNK));
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) layers.get(4).add(new Cell(x + dx, base + 4, z + dz, LEAVES));
                layers.get(5).add(new Cell(x + dx, base + 5, z + dz, LEAVES));
                if (dx == 0 || dz == 0) layers.get(6).add(new Cell(x + dx, base + 6, z + dz, LEAVES));
            }
            crowns.add(new Vector3d(x + 0.5, base + 5, z + 0.5));
        }
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int r = (int) Math.ceil(RADIUS);
        for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {   // haie de ronces sur le pourtour
            double d = Math.hypot(dx, dz);
            if (d < BRAMBLE_MIN || d > RADIUS || rng.nextDouble() > BRAMBLE_CHANCE) continue;
            int x = cx + dx, z = cz + dz, y = floor(world, x, cy, z);
            BlockType below = WorldBlockUtil.getBlockType(world, x, y - 1, z);
            if (below != null && below.getId().startsWith("Soil_")) layers.get(0).add(new Cell(x, y, z, BRAMBLE_BLOCK));
        }
        long now = System.currentTimeMillis();
        SpellFx.fx(FX_DUST, center, 2.0f, store, cb);
        SpellFx.sound(SFX_GROW, center, cb);
        return new Grove(world, new Vector3d(cx + 0.5, cy, cz + 0.5), layers, crowns, TerrainRestore.open(world, now + DURATION_MS), now);
    }

    /** Premiere case libre au-dessus du sol local (de 3 au-dessus a 4 en dessous), sinon la hauteur visee. */
    private static int floor(World world, int x, int y0, int z) {
        for (int y = y0 + 3; y >= y0 - 4; y--) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
            if (t != null && t.getMaterial() == BlockMaterial.Solid && above != null && above.getMaterial() != BlockMaterial.Solid) return y + 1;
        }
        return y0;
    }

    private static final class Grove implements Instance {
        final World world;
        final Vector3d center;
        final List<List<Cell>> layers;
        final List<Vector3d> crowns;
        final TerrainRestore.Restore batch;
        final long startAt;
        int grown;
        long lastLayer, lastPulse, lastCanopy;

        Grove(World world, Vector3d center, List<List<Cell>> layers, List<Vector3d> crowns, TerrainRestore.Restore batch, long startAt) {
            this.world = world; this.center = center; this.layers = layers; this.crowns = crowns; this.batch = batch; this.startAt = startAt;
            this.lastLayer = startAt - LAYER_MS; this.lastCanopy = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) { wither(store, cb); return true; }
            if (grown < layers.size() && now - lastLayer >= LAYER_MS) grow(now, store, cb);
            if (now - lastCanopy >= CANOPY_FX_EVERY_MS) {
                lastCanopy = now;
                for (Vector3d c : crowns) SpellFx.fx(FX_LEAVES, c, 1.4f, store, cb);
            }
            if (now - lastPulse >= PULSE_EVERY_MS) {
                lastPulse = now;
                boolean fxNow = (now - startAt) % 1_000 < PULSE_EVERY_MS;
                for (Ref<EntityStore> a : SpellFx.alliesAround(caster, center, RADIUS + 2, store)) {   // les arbres soignent
                    Vector3d p = SpellFx.position(a, store);
                    if (p == null || !inside(p)) continue;
                    SpellFx.restore(a, DefaultEntityStatTypes.getHealth(), HEAL, cb);
                    if (fxNow) SpellFx.fx(FX_HEAL, p, 0.6f, store, cb);
                }
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + 2, store)) {  // les ronces blessent
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || !inside(p)) continue;
                    SpellFx.damage(caster, e, "Physical", DAMAGE, null, null, cb);
                    SpellFx.effect(e, BRAMBLE, cb);
                    if (fxNow) { SpellFx.fx(FX_DIRT, p, 0.7f, store, cb); SpellFx.sound(SFX_THORN, p, cb); }
                }
            }
            return false;
        }

        private boolean inside(Vector3d p) {
            return SpellFx.horizontalDistance(p, center) <= RADIUS && Math.abs(p.y - center.y) <= 3.5;
        }

        /** Pose la couche suivante des arbres (et la haie avec la premiere). */
        private void grow(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            lastLayer = now;
            List<Cell> layer = layers.get(grown++);
            boolean last = grown == layers.size();
            world.execute(() -> {
                try {
                    String w = world.getName();
                    for (Cell c : layer) {
                        if (TerrainRestore.isEmpty(world, c.x(), c.y(), c.z()) && !TerrainRestore.isProtectedArea(w, c.x(), c.y(), c.z())
                                && WorldBlockUtil.getFluidIndex(world, c.x(), c.y(), c.z()) <= 0)
                            TerrainRestore.place(world, batch, c.x(), c.y(), c.z(), TerrainRestore.EMPTY, c.id(), 0);
                    }
                    if (last) TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("Bosquet: pousse", e);
                }
            });
            for (Vector3d c : crowns) SpellFx.fx(FX_LEAVES, new Vector3d(c.x, c.y - 5 + grown, c.z), 0.8f, store, cb);
            if (last) for (Vector3d c : crowns) SpellFx.sound(SFX_CREAK, c, cb);
        }

        private void wither(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            world.execute(() -> { try { TerrainRestore.restoreNow(world, batch); TerrainRestore.save(); } catch (Exception e) { EldaniorLogger.error("Bosquet: fin", e); } });
            for (Vector3d c : crowns) SpellFx.fx(FX_LEAVES, c, 2.0f, store, cb);
            SpellFx.sound(SFX_END, center, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }

        @Override
        public void abort() { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }
    }
}
