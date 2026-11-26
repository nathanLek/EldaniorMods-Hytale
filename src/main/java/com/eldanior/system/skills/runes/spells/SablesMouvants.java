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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune des Sables Mouvants (Terre, Rare, rune de predilection de l'Alchimiste) : la surface du point vise (rayon 4,
 * 20 blocs) devient du sable 6 s. Ennemis dedans : Elda_Enlisement (x0,3, reapplique toutes les 0,5 s), 10 degats toutes
 * les 0,5 s ; apres 2 s dans la zone, englues 1 s (lockInPlace, une fois par ennemi). Sol restaure a la fin.
 * Fiche de conception : scripts/runes/specs/Rune_SablesMouvants.json
 */
public final class SablesMouvants implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, PULSE_EVERY_MS = 500, STUCK_AFTER_MS = 2_000, STUCK_MS = 1_000;
    private static final double RANGE = 20.0, RADIUS = 4.0;
    private static final float DAMAGE = 10f;

    // ===== Assets =====
    private static final String SAND = "Soil_Sand", BOGGED = "Elda_Enlisement";
    private static final String FX_DUST = "Gaia_Dust_Burst";
    private static final String SFX_SAND = "SFX_Golem_Sand_Slam";

    @Override public String id() { return "SablesMouvants"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        if (SpellFx.position(caster, store) == null || world == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        long now = System.currentTimeMillis();
        TerrainRestore.Restore batch = TerrainRestore.open(world, now + DURATION_MS);
        int cx = (int) Math.floor(center.x), cy = (int) Math.floor(center.y), cz = (int) Math.floor(center.z), r = (int) Math.ceil(RADIUS);
        world.execute(() -> {                                                    // la surface se change en sable
            try {
                for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
                    if (Math.hypot(dx, dz) > RADIUS) continue;
                    int x = cx + dx, z = cz + dz;
                    for (int y = cy + 1; y >= cy - 3; y--) {
                        BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
                        if (t == null || t.getMaterial() != BlockMaterial.Solid || above == null || above.getMaterial() == BlockMaterial.Solid) continue;
                        String id = t.getId();
                        if (!id.equals(SAND) && !TerrainRestore.isKept(world, id, x, y, z) && !TerrainRestore.isProtectedArea(world.getName(), x, y, z)
                                && WorldBlockUtil.getFluidIndex(world, x, y + 1, z) <= 0)
                            TerrainRestore.place(world, batch, x, y, z, id, SAND, 0);
                        break;
                    }
                }
                TerrainRestore.save();
            } catch (Exception e) {
                EldaniorLogger.error("SablesMouvants: sol", e);
            }
        });
        SpellFx.fx(FX_DUST, center, 2.5f, store, cb);
        SpellFx.sound(SFX_SAND, center, cb);
        return new Quicksand(center, world, batch, now);
    }

    private static final class Quicksand implements Instance {
        final Vector3d center;
        final World world;
        final TerrainRestore.Restore batch;
        final long startAt;
        final Map<Ref<EntityStore>, Long> enteredAt = new HashMap<>();
        final Set<Ref<EntityStore>> stuck = new HashSet<>();
        long lastPulse;

        Quicksand(Vector3d center, World world, TerrainRestore.Restore batch, long startAt) { this.center = center; this.world = world; this.batch = batch; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastPulse < PULSE_EVERY_MS) return false;
            lastPulse = now;
            Set<Ref<EntityStore>> inside = new HashSet<>();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + 2, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, center) > RADIUS || Math.abs(p.y - center.y) > 2.5) continue;
                inside.add(e);
                long since = enteredAt.computeIfAbsent(e, k -> now);
                SpellFx.effect(e, BOGGED, cb);
                SpellFx.damage(caster, e, "Earth", DAMAGE, null, null, cb);
                if (now - since >= STUCK_AFTER_MS && stuck.add(e)) SpellFx.lockInPlace(e, STUCK_MS, store);   // englue
            }
            enteredAt.keySet().retainAll(inside);
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (int i = 0; i < 2; i++) {                                         // remous de sable
                double a = rng.nextDouble(Math.PI * 2), r = RADIUS * Math.sqrt(rng.nextDouble());
                SpellFx.fx(FX_DUST, new Vector3d(center).add(Math.cos(a) * r, 0.1, Math.sin(a) * r), 0.7f, store, cb);
            }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }

        @Override
        public void abort() { world.execute(() -> TerrainRestore.restoreNow(world, batch)); }
    }
}
