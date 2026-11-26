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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de la Maree Gravitationnelle (Spatial, Legendaire, rune de predilection du Demiurge Supreme) : 20 s autour de la
 * position d'incantation (rayon 25), 5 cycles de 4 s. Maree basse (2 s) : ennemis souleves a leur sol + 4 (vitesse
 * verticale imposee), jusqu'a 8 blocs de surface arraches montent avec eux. Maree haute : tout retombe, ennemis plaques
 * (-25) : 60 ; les debris retombent puis disparaissent ; la cuvette centrale (r 6, en cone) se creuse d'une couche.
 * Lanceur et allies : protection de chute. Terrain restaure a la fin. Gravite invisible : pas de poussiere.
 * Fiche de conception : scripts/runes/specs/Rune_MareeGravitationnelle.json
 */
public final class MareeGravitationnelle implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 20_000, CYCLE_MS = 4_000, LOW_MS = 2_000, FALL_MS = 400, GUARD_MS = 4_500;
    private static final double RADIUS = 25.0, HEIGHT = 8.0, HOVER = 4.0, RISE_GAIN = 5.0, RISE_MAX = 9.0, SLAM = 25.0;
    private static final double BOWL_RADIUS = 6.0, BOWL_SHRINK = 1.2, DEBRIS_RISE = 4.0;
    private static final int MAX_LIFTED = 20, DEBRIS_PER_TIDE = 8, MAX_DEBRIS = 40, BOWL_LAYERS = 4;
    private static final float SLAM_DAMAGE = 60f;

    // ===== Assets =====
    private static final String FX_COLUMN = "Gravite_Colonne_Legere";
    private static final String SFX_OPEN = "SFX_Portal_Void", SFX_SLAM = "SFX_Golem_Earth_Slam_Impact";

    @Override public String id() { return "MareeGravitationnelle"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null || world == null) return null;
        SpellFx.sound(SFX_OPEN, pos, cb);
        long now = System.currentTimeMillis();
        return new Tide(world, new Vector3d(pos), TerrainRestore.open(world, now + DURATION_MS + 3_000), now);
    }

    private static final class Debris {
        final Ref<EntityStore> ref;
        final Vector3d from;
        final long born;
        long fallAt;
        Debris(Ref<EntityStore> ref, Vector3d from, long born) { this.ref = ref; this.from = from; this.born = born; }
    }

    private static final class Tide implements Instance {
        final World world;
        final Vector3d center;
        final TerrainRestore.Restore batch;
        final long startAt;
        final Map<Ref<EntityStore>, Double> lifted = new HashMap<>();
        final List<Debris> debris = new ArrayList<>();
        int cycle = -1, debrisSpawned, bowlLayers;
        boolean high;

        Tide(World world, Vector3d center, TerrainRestore.Restore batch, long startAt) { this.world = world; this.center = center; this.batch = batch; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            long t = now - startAt;
            if (t >= DURATION_MS) { finish(); return true; }
            int c = (int) (t / CYCLE_MS);
            boolean low = t % CYCLE_MS < LOW_MS;
            if (c != cycle) { cycle = c; high = false; ebb(caster, now, store, cb); }   // maree basse
            if (!low && !high) { high = true; flood(caster, now, store, cb); }          // maree haute
            if (low) levitate(store, cb);
            animate(now, store, cb);
            return false;
        }

        private boolean inside(Vector3d p) { return SpellFx.horizontalDistance(p, center) <= RADIUS && Math.abs(p.y - center.y) <= HEIGHT; }

        /** Maree basse : les ennemis s'elevent, des blocs du sol montent avec eux ; allies proteges des chutes. */
        private void ebb(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> a : SpellFx.alliesAround(caster, center, RADIUS + HEIGHT, store)) SpellFx.protectFromFall(a, GUARD_MS, store);
            lifted.clear();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + HEIGHT, store)) {
                if (lifted.size() >= MAX_LIFTED || RuneSpellRuntime.isSummon(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p != null && inside(p)) lifted.put(e, p.y);
            }
            rip(now, store, cb);
        }

        private void levitate(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Map.Entry<Ref<EntityStore>, Double> en : lifted.entrySet()) {
                Vector3d p = SpellFx.position(en.getKey(), store);
                if (p == null) continue;
                double vy = Math.max(-RISE_MAX, Math.min(RISE_MAX, (en.getValue() + HOVER - p.y) * RISE_GAIN));
                SpellFx.push(en.getKey(), new Vector3d(0, vy, 0), false, cb);
            }
        }

        /** Maree haute : tout est plaque, les debris retombent, la cuvette se creuse. */
        private void flood(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + HEIGHT + HOVER, store)) {
                if (RuneSpellRuntime.isSummon(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, center) > RADIUS) continue;
                SpellFx.damage(caster, e, "Crush", SLAM_DAMAGE, new Vector3d(0, -SLAM, 0), "Impact_Strong", cb);
            }
            lifted.clear();
            for (Debris d : debris) if (d.fallAt == 0) d.fallAt = now;
            SpellFx.fx(FX_COLUMN, center, 1.0f, store, cb);
            SpellFx.sound(SFX_SLAM, center, cb);
            SpellFx.shake(center, RADIUS + 10, "Impact_Strong", store);
            if (bowlLayers < BOWL_LAYERS) dig(bowlLayers++);
        }

        /** Couche k de la cuvette : un disque qui retrecit a chaque profondeur (cone). */
        private void dig(int k) {
            int cx = (int) Math.floor(center.x), cz = (int) Math.floor(center.z), y = (int) Math.floor(center.y) - 1 - k;
            double r = BOWL_RADIUS - k * BOWL_SHRINK;
            int ri = (int) Math.ceil(r);
            world.execute(() -> {
                try {
                    String w = world.getName();
                    for (int dx = -ri; dx <= ri; dx++) for (int dz = -ri; dz <= ri; dz++) {
                        if (Math.hypot(dx, dz) > r) continue;
                        int x = cx + dx, z = cz + dz;
                        BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                        if (t == null || t.getMaterial() != BlockMaterial.Solid) continue;
                        String id = t.getId();
                        if (TerrainRestore.isKept(world, id, x, y, z) || TerrainRestore.isProtectedArea(w, x, y, z) || WorldBlockUtil.getFluidIndex(world, x, y + 1, z) > 0) continue;
                        TerrainRestore.place(world, batch, x, y, z, id, TerrainRestore.EMPTY, 0);
                    }
                    TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("MareeGravitationnelle: cuvette", e);
                }
            });
        }

        /** Arrache quelques blocs de surface dans la zone (case videe, restauree a la fin). */
        private void rip(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            List<int[]> cells = new ArrayList<>();
            for (int i = 0; i < DEBRIS_PER_TIDE * 2 && cells.size() < DEBRIS_PER_TIDE && debrisSpawned < MAX_DEBRIS; i++) {
                double a = rng.nextDouble(Math.PI * 2), r = BOWL_RADIUS + 1 + (RADIUS - BOWL_RADIUS - 1) * Math.sqrt(rng.nextDouble());
                int x = (int) Math.floor(center.x + Math.cos(a) * r), z = (int) Math.floor(center.z + Math.sin(a) * r);
                int y = surface(x, (int) Math.floor(center.y), z);
                if (y == Integer.MIN_VALUE) continue;
                Vector3d at = new Vector3d(x + 0.5, y + 0.5, z + 0.5);
                Ref<EntityStore> ref = SpellFx.spawnBlock(WorldBlockUtil.getBlockType(world, x, y, z).getId(), at, store, cb);
                if (ref == null) continue;
                debris.add(new Debris(ref, at, now));
                debrisSpawned++;
                cells.add(new int[]{x, y, z});
            }
            if (cells.isEmpty()) return;
            world.execute(() -> {
                try {
                    for (int[] c : cells) {
                        BlockType t = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                        if (t != null) TerrainRestore.place(world, batch, c[0], c[1], c[2], t.getId(), TerrainRestore.EMPTY, 0);
                    }
                    TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("MareeGravitationnelle: debris", e);
                }
            });
        }

        private int surface(int x, int y0, int z) {
            for (int y = y0 + 4; y >= y0 - 6; y--) {
                BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
                if (t == null || above == null) return Integer.MIN_VALUE;
                if (t.getMaterial() != BlockMaterial.Solid || above.getMaterial() == BlockMaterial.Solid) continue;
                String id = t.getId();
                if (id.startsWith("Plant_") || TerrainRestore.isKept(world, id, x, y, z) || TerrainRestore.isProtectedArea(world.getName(), x, y, z)
                        || WorldBlockUtil.getFluidIndex(world, x, y + 1, z) > 0) return Integer.MIN_VALUE;
                return y;
            }
            return Integer.MIN_VALUE;
        }

        /** Debris : montent en douceur pendant la maree basse, retombent en 0,4 s a la maree haute, puis disparaissent. */
        private void animate(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            debris.removeIf(d -> {
                double y;
                if (d.fallAt == 0) {
                    double tau = Math.min(1.0, (now - d.born) / (double) LOW_MS);
                    y = DEBRIS_RISE * (1 - (1 - tau) * (1 - tau));
                } else {
                    double tau = (now - d.fallAt) / (double) FALL_MS;
                    if (tau >= 1) { SpellFx.remove(d.ref, cb); return true; }
                    y = DEBRIS_RISE * (1 - tau * tau);
                }
                SpellFx.hold(d.ref, new Vector3d(d.from).add(0, y, 0), null, store);
                return false;
            });
        }

        private void finish() { world.execute(() -> { try { TerrainRestore.restoreNow(world, batch); TerrainRestore.save(); } catch (Exception e) { EldaniorLogger.error("MareeGravitationnelle: fin", e); } }); }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Debris d : debris) SpellFx.remove(d.ref, cb);
            debris.clear();
            finish();
        }

        @Override
        public void abort() {
            for (Debris d : debris) RuneSpellRuntime.removeLater(d.ref);
            finish();
        }
    }
}
