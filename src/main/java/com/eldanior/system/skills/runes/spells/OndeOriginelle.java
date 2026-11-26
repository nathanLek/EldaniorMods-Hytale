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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de l'Onde Originelle (Spatial, Unique, rune de predilection de la Force Originelle) : une onde part du lanceur
 * a 12 b/s jusqu'a 25 blocs. Ennemis traverses : 150, souleves a leur sol + 4 (vitesse verticale imposee) pendant 1,5 s,
 * puis plaques (-25) : 200. Sur le front, jusqu'a 6 blocs de surface tous les 0,15 s sont arraches (case videe, entite-bloc
 * qui monte de 3 blocs puis retombe en 1,2 s) ; le terrain est restaure a la fin. Gravite invisible : pas de poussiere.
 * Fiche de conception : scripts/runes/specs/Rune_OndeOriginelle.json
 */
public final class OndeOriginelle implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long FRONT_EVERY_MS = 150, HOVER_MS = 1_500, DEBRIS_MS = 1_200, SAFETY_MS = 10_000;
    private static final double RADIUS = 25.0, SPEED = 12.0, START = 1.5, FRONT_WIDTH = 2.0, HOVER = 4.0, RISE_GAIN = 5.0, RISE_MAX = 9.0;
    private static final double SLAM = 25.0, DEBRIS_PEAK = 3.0, HEIGHT = 4.0;
    private static final int DEBRIS_PER_STEP = 6, DEBRIS_MAX = 90;
    private static final float WAVE = 150f, SLAM_DAMAGE = 200f;

    // ===== Assets =====
    private static final String SFX_WAVE = "SFX_Golem_Earth_Stomp_Impact", SFX_SLAM = "SFX_Golem_Earth_Slam_Impact";

    @Override public String id() { return "OndeOriginelle"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null || world == null) return null;
        SpellFx.sound(SFX_WAVE, pos, cb);
        SpellFx.shake(pos, RADIUS + 10, "Impact_Strong", store);
        long now = System.currentTimeMillis();
        return new Wave(world, new Vector3d(pos), TerrainRestore.open(world, now + SAFETY_MS), now);
    }

    private record Debris(Ref<EntityStore> ref, Vector3d from, long born) {}

    private static final class Lifted {
        final double groundY;
        final long at;
        Lifted(double groundY, long at) { this.groundY = groundY; this.at = at; }
    }

    private static final class Wave implements Instance {
        final World world;
        final Vector3d origin;
        final TerrainRestore.Restore batch;
        final long startAt;
        final Set<Ref<EntityStore>> hit = new HashSet<>();
        final Map<Ref<EntityStore>, Lifted> lifted = new HashMap<>();
        final List<Debris> debris = new ArrayList<>();
        int debrisSpawned;
        long lastFront;

        Wave(World world, Vector3d origin, TerrainRestore.Restore batch, long startAt) { this.world = world; this.origin = origin; this.batch = batch; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            double r = START + SPEED * (now - startAt) / 1000.0;
            boolean front = r <= RADIUS;
            if (front) strike(caster, r, now, store, cb);
            if (front && now - lastFront >= FRONT_EVERY_MS) { lastFront = now; rip(r, now, store, cb); }
            levitate(caster, now, store, cb);
            animate(now, store, cb);
            if (!front && lifted.isEmpty() && debris.isEmpty()) { restore(); return true; }
            return false;
        }

        /** Le front frappe et souleve les ennemis qu'il traverse. */
        private void strike(Ref<EntityStore> caster, double r, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, origin, r + HEIGHT, store)) {
                if (hit.contains(e) || RuneSpellRuntime.isSummon(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                double d = SpellFx.horizontalDistance(p, origin);
                if (d > r || d < r - FRONT_WIDTH - 1 || Math.abs(p.y - origin.y) > HEIGHT) continue;
                hit.add(e);
                SpellFx.damage(caster, e, "Crush", WAVE, new Vector3d(0, RISE_MAX, 0), "Impact_Light", cb);
                lifted.put(e, new Lifted(p.y, now));
            }
        }

        /** Les souleves flottent a leur sol + 4, puis la gravite revient d'un coup. */
        private void levitate(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            lifted.entrySet().removeIf(en -> {
                Ref<EntityStore> e = en.getKey();
                Vector3d p = SpellFx.position(e, store);
                if (p == null) return true;
                if (now - en.getValue().at >= HOVER_MS) {
                    SpellFx.damage(caster, e, "Crush", SLAM_DAMAGE, new Vector3d(0, -SLAM, 0), "Impact_Strong", cb);
                    SpellFx.sound(SFX_SLAM, p, cb);
                    return true;
                }
                double vy = Math.max(-RISE_MAX, Math.min(RISE_MAX, (en.getValue().groundY + HOVER - p.y) * RISE_GAIN));
                SpellFx.push(e, new Vector3d(0, vy, 0), false, cb);
                return false;
            });
        }

        /** Arrache quelques blocs de surface sur le front (case videe, restauree a la fin). */
        private void rip(double r, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (debrisSpawned >= DEBRIS_MAX) return;
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            List<int[]> cells = new ArrayList<>();
            for (int i = 0; i < DEBRIS_PER_STEP && debrisSpawned < DEBRIS_MAX; i++) {
                double a = rng.nextDouble(Math.PI * 2);
                int x = (int) Math.floor(origin.x + Math.cos(a) * r), z = (int) Math.floor(origin.z + Math.sin(a) * r);
                int y = surface(x, (int) Math.floor(origin.y), z);
                if (y == Integer.MIN_VALUE) continue;
                BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                if (t == null) continue;
                Vector3d at = new Vector3d(x + 0.5, y + 0.5, z + 0.5);
                Ref<EntityStore> ref = SpellFx.spawnBlock(t.getId(), at, store, cb);
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
                    EldaniorLogger.error("OndeOriginelle: sol", e);
                }
            });
        }

        /** Bloc de surface arrachable sur la colonne (solide, air au-dessus, ni plante ni fluide ni zone protegee). */
        private int surface(int x, int y0, int z) {
            for (int y = y0 + 3; y >= y0 - 4; y--) {
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

        /** Les blocs arraches montent puis retombent (parabole), puis disparaissent. */
        private void animate(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            debris.removeIf(d -> {
                double tau = (now - d.born()) / (double) DEBRIS_MS;
                if (tau >= 1) { SpellFx.remove(d.ref(), cb); return true; }
                SpellFx.hold(d.ref(), new Vector3d(d.from()).add(0, DEBRIS_PEAK * 4 * tau * (1 - tau), 0), null, store);
                return false;
            });
        }

        private void restore() { world.execute(() -> { try { TerrainRestore.restoreNow(world, batch); TerrainRestore.save(); } catch (Exception e) { EldaniorLogger.error("OndeOriginelle: fin", e); } }); }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Debris d : debris) SpellFx.remove(d.ref(), cb);
            debris.clear();
            restore();
        }

        @Override
        public void abort() {
            for (Debris d : debris) RuneSpellRuntime.removeLater(d.ref());
            restore();
        }
    }
}
