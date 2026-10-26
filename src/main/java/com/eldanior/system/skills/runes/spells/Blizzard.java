package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Rune du Blizzard (Glace, Epic) : tempete de neige de 10 s sur la zone visee (rayon 8). Degats et ralentissement en
 * continu, gel 2 s apres 3 s cumulees dans la tempete (une fois par ennemi). Le sol naturel se couvre de neige
 * (300 blocs max, restauree apres 60 s).
 * Fiche de conception : scripts/runes/specs/Rune_Blizzard.json
 */
public final class Blizzard implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, PULSE_EVERY_MS = 500, FLURRY_EVERY_MS = 2_500, HOWL_EVERY_MS = 3_000;
    private static final long FREEZE_AFTER_MS = 3_000, FREEZE_MS = 2_000, RESTORE_AFTER_MS = 60_000;
    private static final double TARGET_RANGE = 25.0, FALLBACK = 10.0, RADIUS = 8.0, HEIGHT = 6.0;
    private static final float DAMAGE = 8f;
    private static final int MAX_BLOCKS = 300;

    // ===== Assets =====
    private static final String SNOW = "Soil_Snow";
    private static final String SLOW = "Elda_Givre", FREEZE = "Elda_Gel";
    private static final String FX_RING = "Niflheim_Ring", FX_FLURRY = "Niflheim_Flurry", FX_FREEZE = "Ability_Ice_Impact";
    private static final String SFX_START = "SFX_Ice_Build", SFX_HOWL = "SFX_Golem_Frost_Spin";

    @Override public String id() { return "Blizzard"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        World world = SpellFx.world(store);
        Vector3d center = SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK);
        long now = System.currentTimeMillis();
        TerrainRestore.Restore terrain = TerrainRestore.open(world, now + DURATION_MS + RESTORE_AFTER_MS);
        snow(world, terrain, center);
        SpellFx.fx(FX_RING, new Vector3d(center).add(0, 0.2, 0), (float) (RADIUS / 20.0), store, cb);
        SpellFx.sound(SFX_START, center, cb);
        return new Storm(center, terrain, now);
    }

    /** Recouvre de neige la surface naturelle (Soil_*) du disque, hors zones protegees et sous l'eau. */
    private static void snow(World world, TerrainRestore.Restore terrain, Vector3d c) {
        int r = (int) Math.ceil(RADIUS), cx = (int) Math.floor(c.x), cy = (int) Math.floor(c.y), cz = (int) Math.floor(c.z);
        world.execute(() -> {
            try {
                int placed = 0;
                for (int dx = -r; dx <= r && placed < MAX_BLOCKS; dx++) {
                    for (int dz = -r; dz <= r && placed < MAX_BLOCKS; dz++) {
                        if (dx * dx + dz * dz > RADIUS * RADIUS) continue;
                        int x = cx + dx, z = cz + dz;
                        int top = GraviteSol.surface(world, x, cy, z);
                        if (top == Integer.MIN_VALUE || !GraviteSol.editable(world, x, top, z)) continue;
                        String id = WorldBlockUtil.getBlockType(world, x, top, z).getId();
                        if (!id.startsWith("Soil_") || id.startsWith("Soil_Snow")) continue;
                        if (WorldBlockUtil.getFluidIndex(world, x, top + 1, z) > 0) continue;
                        TerrainRestore.place(world, terrain, x, top, z, id, SNOW, 0);
                        placed++;
                    }
                }
                TerrainRestore.save();
            } catch (Exception e) {
                EldaniorLogger.error("Blizzard: neige", e);
            }
        });
    }

    private static final class Storm implements Instance {
        final Vector3d center;
        final TerrainRestore.Restore terrain;
        final long startAt;
        final Map<Ref<EntityStore>, Long> inside = new HashMap<>();   // temps cumule dans la tempete
        final Set<Ref<EntityStore>> frozen = new HashSet<>();
        long lastPulse, lastFlurry = Long.MIN_VALUE / 2, lastHowl;

        Storm(Vector3d center, TerrainRestore.Restore terrain, long startAt) {
            this.center = center; this.terrain = terrain; this.startAt = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastFlurry >= FLURRY_EVERY_MS) {                               // bourrasques de neige en grille sur la zone
                lastFlurry = now;
                for (double dx = -RADIUS + 2; dx <= RADIUS - 2; dx += 4)
                    for (double dz = -RADIUS + 2; dz <= RADIUS - 2; dz += 4)
                        if (dx * dx + dz * dz <= RADIUS * RADIUS) SpellFx.fx(FX_FLURRY, new Vector3d(center).add(dx, 4, dz), 0.8f, store, cb);
            }
            if (now - lastHowl >= HOWL_EVERY_MS) { lastHowl = now; SpellFx.sound(SFX_HOWL, center, cb); }
            if (now - lastPulse >= PULSE_EVERY_MS) {
                lastPulse = now;
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + 1, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || SpellFx.horizontalDistance(p, center) > RADIUS || p.y < center.y - 3 || p.y > center.y + HEIGHT) continue;
                    SpellFx.damage(caster, e, "Ice", DAMAGE, null, null, cb);
                    SpellFx.effect(e, SLOW, cb);
                    long total = inside.merge(e, PULSE_EVERY_MS, Long::sum);
                    if (total >= FREEZE_AFTER_MS && frozen.add(e)) {
                        SpellFx.stun(e, FREEZE, FREEZE_MS, store, cb);
                        SpellFx.fx(FX_FREEZE, p.add(0, 1, 0), 1.0f, store, cb);
                    }
                }
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
