package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
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
 * Rune du Sentier d'Hiver (Glace, Rare) : pendant 6 s, la neige recouvre le sol sous les pas du lanceur ; les ennemis
 * qui foulent la trainee sont blesses et ralentis, puis geles apres 2 s cumulees (une fois chacun). Neige restauree
 * apres 30 s, sols naturels seulement, 50 blocs max ; la trainee agit meme la ou la neige ne peut pas etre posee.
 * Fiche de conception : scripts/runes/specs/Rune_SentierDHiver.json
 */
public final class SentierDHiver implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, STEP_EVERY_MS = 150, PULSE_EVERY_MS = 500, MIST_EVERY_MS = 2_000;
    private static final long FREEZE_AFTER_MS = 2_000, FREEZE_MS = 1_500, RESTORE_AFTER_MS = 30_000;
    private static final double CELL_REACH = 1.2, SCAN_RADIUS = 40.0;
    private static final float DAMAGE = 8f;
    private static final int MAX_BLOCKS = 50, MAX_CELLS = 120;

    // ===== Assets =====
    private static final String SNOW = "Soil_Snow";
    private static final String SLOW = "Elda_Givre", FREEZE = "Elda_Gel";
    private static final String FX_STEP = "Effect_Snow_Impact", FX_MIST = "Niflheim_Mist", FX_FREEZE = "Ability_Ice_Impact";
    private static final String SFX_START = "SFX_Ice_Build";

    @Override public String id() { return "SentierDHiver"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.fx(FX_MIST, pos, 0.8f, store, cb);
        SpellFx.sound(SFX_START, pos, cb);
        long now = System.currentTimeMillis();
        return new Trail(caster, SpellFx.world(store), now);
    }

    private static final class Trail implements Instance {
        final Ref<EntityStore> caster;
        final World world;
        final long startAt;
        final TerrainRestore.Restore terrain;
        final List<int[]> cells = new ArrayList<>();                   // cases de la trainee {x, y du sol, z}
        final Set<Long> known = new HashSet<>();
        final Map<Ref<EntityStore>, Long> onTrail = new HashMap<>();   // temps cumule sur la neige
        final Set<Ref<EntityStore>> frozen = new HashSet<>();
        long lastStep, lastPulse, lastMist;
        int placed;

        Trail(Ref<EntityStore> caster, World world, long startAt) {
            this.caster = caster; this.world = world; this.startAt = startAt; this.lastMist = startAt;
            this.terrain = TerrainRestore.open(world, startAt + DURATION_MS + RESTORE_AFTER_MS);
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            if (now - lastStep >= STEP_EVERY_MS) { lastStep = now; step(pos); }
            if (now - lastMist >= MIST_EVERY_MS) { lastMist = now; SpellFx.fx(FX_MIST, pos, 0.6f, store, cb); }
            if (now - lastPulse >= PULSE_EVERY_MS) { lastPulse = now; pulse(pos, store, cb); }
            return false;
        }

        /** Ajoute la case sous les pieds a la trainee et la recouvre de neige si c'est un sol naturel. */
        private void step(Vector3d pos) {
            int x = (int) Math.floor(pos.x), y = (int) Math.floor(pos.y) - 1, z = (int) Math.floor(pos.z);
            long key = ((long) x << 40) ^ ((long) (y & 0xFFFFF) << 20) ^ (z & 0xFFFFF);
            if (cells.size() >= MAX_CELLS || !known.add(key)) return;
            cells.add(new int[]{x, y, z});
            if (placed >= MAX_BLOCKS) return;
            placed++;
            world.execute(() -> {
                try {
                    if (TerrainRestore.isProtectedArea(world.getName(), x, y, z)) return;
                    BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                    if (t == null || TerrainRestore.isKept(world, t.getId(), x, y, z)) return;
                    String id = t.getId();
                    if (!id.startsWith("Soil_") || id.startsWith("Soil_Snow")) return;
                    if (WorldBlockUtil.getFluidIndex(world, x, y + 1, z) > 0) return;
                    TerrainRestore.place(world, terrain, x, y, z, id, SNOW, 0);
                } catch (Exception e) {
                    EldaniorLogger.error("SentierDHiver: neige", e);
                }
            });
        }

        /** Givre sur la trainee, puis degats, ralentissement et gel des ennemis qui la foulent. */
        private void pulse(Vector3d pos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (cells.isEmpty()) return;
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (int i = 0; i < 4; i++) {
                int[] c = cells.get(rng.nextInt(cells.size()));
                SpellFx.fx(FX_STEP, new Vector3d(c[0] + 0.5, c[1] + 1.1, c[2] + 0.5), 0.8f, store, cb);
            }
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, SCAN_RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || !onCell(p)) continue;
                SpellFx.damage(caster, e, "Ice", DAMAGE, null, null, cb);
                SpellFx.effect(e, SLOW, cb);
                long total = onTrail.merge(e, PULSE_EVERY_MS, Long::sum);
                if (total >= FREEZE_AFTER_MS && frozen.add(e)) {
                    EffectsManager.applyCustomEffect(e, FREEZE, FREEZE_MS / 1000f, OverlapBehavior.OVERWRITE, cb);
                    SpellFx.lockInPlace(e, FREEZE_MS, store);
                    SpellFx.fx(FX_FREEZE, new Vector3d(p).add(0, 1, 0), 1.0f, store, cb);
                }
            }
        }

        private boolean onCell(Vector3d p) {
            for (int[] c : cells) {
                double dx = p.x - (c[0] + 0.5), dz = p.z - (c[2] + 0.5), dy = p.y - (c[1] + 1);
                if (dx * dx + dz * dz <= CELL_REACH * CELL_REACH && dy > -0.6 && dy < 1.5) return true;
            }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            TerrainRestore.save();
        }

        @Override
        public void abort() {
            TerrainRestore.save();
        }
    }
}
