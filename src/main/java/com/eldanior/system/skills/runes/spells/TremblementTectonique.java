package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune du Tremblement Tectonique (Terre, Unique) : quatre seismes en chaine qui avancent droit devant (tous les 5 blocs,
 * un toutes les 0,6 s). Chacun ouvre des fissures de basalte (restaurees apres 2 min) et frappe, projette et etourdit
 * les ennemis a moins de 6 blocs.
 * Fiche de conception : scripts/runes/specs/Rune_TremblementTectonique.json
 */
public final class TremblementTectonique implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final int QUAKES = 4, CRACKS = 5;
    private static final double FIRST = 4.0, SPACING = 5.0, RADIUS = 6.0;
    private static final double CRACK_MIN = 3.0, CRACK_MAX = 5.5, CRACK_DEPTH_START = 1.0, CRACK_DEPTH_END = 0.5, BRANCH_CHANCE = 0.15;
    private static final long QUAKE_EVERY_MS = 600, STUN_MS = 1_500, RESTORE_AFTER_MS = 120_000;
    private static final float DAMAGE = 130f;
    private static final double PUSH = 6.0, LIFT = 8.0;

    // ===== Assets =====
    private static final String FLOOR = "Rock_Basalt", FLOOR_HALF = "Rock_Basalt_Half";
    private static final String STUN = "Gaia_Etourdi";
    private static final String FX_DUST = "Gaia_Dust_Burst";
    private static final String SFX_QUAKE = "SFX_Golem_Frost_Slam_Impact";

    @Override public String id() { return "TremblementTectonique"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d dir = SpellFx.forward(caster, store);
        dir.y = 0;
        if (dir.lengthSquared() < 1e-6) dir.set(0, 0, 1);
        dir.normalize();
        World world = SpellFx.world(store);
        long now = System.currentTimeMillis();
        return new Quakes(pos, dir, world, now);
    }

    private static final class Quakes implements Instance {
        final Vector3d origin, dir;
        final World world;
        final TerrainRestore.Restore terrain;
        final long startAt;
        int done;

        Quakes(Vector3d origin, Vector3d dir, World world, long startAt) {
            this.origin = origin; this.dir = dir; this.world = world; this.startAt = startAt;
            this.terrain = TerrainRestore.open(world, startAt + RESTORE_AFTER_MS);
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt < done * QUAKE_EVERY_MS) return false;
            Vector3d at = SpellFx.ground(world, new Vector3d(origin).add(new Vector3d(dir).mul(FIRST + SPACING * done)));
            quake(caster, at, store, cb);
            return ++done >= QUAKES;
        }

        private void quake(Ref<EntityStore> caster, Vector3d at, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            crack(at);
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            SpellFx.fx(FX_DUST, new Vector3d(at).add(0, 0.5, 0), 1.6f, store, cb);
            for (int i = 0; i < 3; i++) SpellFx.fx(FX_DUST, new Vector3d(at).add(rng.nextDouble(-3, 3), 0.5, rng.nextDouble(-3, 3)), 1.0f, store, cb);
            SpellFx.sound(SFX_QUAKE, at, cb);
            SpellFx.shake(at, 20, "Impact_Strong", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || Math.abs(p.y - at.y) > 4) continue;
                Vector3d out = new Vector3d(p.x - at.x, 0, p.z - at.z);
                out = out.lengthSquared() > 0.25 ? out.normalize().mul(PUSH) : new Vector3d(dir).mul(PUSH);
                SpellFx.damage(caster, e, "Earth", DAMAGE, out.add(0, LIFT, 0), "Impact_Strong", cb);
                SpellFx.stun(e, STUN, STUN_MS, store, cb);
            }
        }

        /** Fissures rayonnantes creusees en basalte autour de l'epicentre (jamais en zone protegee ni sous l'eau). */
        private void crack(Vector3d at) {
            long seed = ThreadLocalRandom.current().nextLong();
            int baseY = (int) Math.floor(at.y) - 1;
            world.execute(() -> {
                try {
                    java.util.Random rng = new java.util.Random(seed);
                    List<double[]> cells = new ArrayList<>();
                    Set<Long> seen = new HashSet<>();
                    for (int k = 0; k < CRACKS; k++)
                        GraviteSol.crackPath(at, Math.PI * 2 * k / CRACKS + rng.nextDouble() * 0.8, 0.5, CRACK_MIN + rng.nextDouble() * (CRACK_MAX - CRACK_MIN),
                                CRACK_DEPTH_START, CRACK_DEPTH_END, BRANCH_CHANCE, cells, seen, rng, true);
                    for (double[] c : cells) {
                        int x = (int) c[0], z = (int) c[1];
                        int top = GraviteSol.surface(world, x, baseY, z);
                        if (top == Integer.MIN_VALUE || TerrainRestore.isProtectedArea(world.getName(), x, top, z) || WorldBlockUtil.getFluidIndex(world, x, top + 1, z) > 0) continue;
                        GraviteSol.dig(world, terrain, x, top, z, c[2], FLOOR, FLOOR_HALF);
                    }
                    TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("TremblementTectonique: fissures", e);
                }
            });
        }

        @Override
        public void abort() {
            TerrainRestore.save();
        }
    }
}
