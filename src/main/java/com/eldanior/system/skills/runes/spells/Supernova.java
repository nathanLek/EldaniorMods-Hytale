package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

/**
 * Rune de la Supernova (Feu, Legendaire, rune de predilection du Createur Cosmique) : 3 s de charge (Elda_Canalisation
 * x0,5, degats subis x0,5) pendant lesquelles les ennemis a 15 blocs sont attires (3 b/s) et brules (15 / 0,5 s). Puis
 * explosion : 600 + Elda_Brulure + recul 18 a 8 blocs ; onde de choc de 8 a 30 blocs (25 b/s) : 250 + recul 10, une fois
 * par ennemi (sauf ceux du coeur). Surface calcinee (Soil_Dirt_Burnt) a 10 blocs, restauree apres 60 s.
 * Fiche de conception : scripts/runes/specs/Rune_Supernova.json
 */
public final class Supernova implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long CHARGE_MS = 3_000, PULL_EVERY_MS = 250, BURN_EVERY_MS = 500, PILLAR_EVERY_MS = 1_000, SCORCH_MS = 60_000;
    private static final double PULL_RADIUS = 15.0, PULL = 3.0, CORE = 8.0, WAVE_END = 30.0, WAVE_SPEED = 25.0, WAVE_WIDTH = 2.5;
    private static final double CORE_PUSH = 18.0, WAVE_PUSH = 10.0, SCORCH_RADIUS = 10.0, HEIGHT = 6.0;
    private static final int RING_POINTS = 12;
    private static final float CHARGE_BURN = 15f, CORE_DAMAGE = 600f, WAVE_DAMAGE = 250f, CHARGE_GUARD = 0.5f;

    // ===== Assets =====
    private static final String CHANNEL = "Elda_Canalisation", BURN = "Elda_Brulure", SCORCHED = "Soil_Dirt_Burnt";
    private static final String FX_FIRE = "Impact_Fire", FX_PILLAR = "Muspelheim_Fire_Pillar", FX_FLASH = "Chaos_Flash", FX_EXPLOSION = "Explosion_Big";
    private static final String FX_BLAST = "Gobliterator_Furnace_Blast", FX_SHOCKWAVE = "Chaos_Shockwave";
    private static final String SFX_CHARGE = "SFX_Gobliterator_Rocket_Barrage_Pre", SFX_BOOM = "SFX_Gobliterator_Rocket_Barrage_Explosion";

    @Override public String id() { return "Supernova"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        EffectsManager.applyCustomEffect(caster, CHANNEL, CHARGE_MS / 1000f, OverlapBehavior.OVERWRITE, cb);
        SpellFx.sound(SFX_CHARGE, pos, cb);
        return new Star(world, System.currentTimeMillis());
    }

    private static final class Star implements Instance {
        final World world;
        final long startAt;
        final Set<Ref<EntityStore>> hit = new HashSet<>();
        Vector3d core;
        long lastPull, lastBurn, lastPillar;

        Star(World world, long startAt) { this.world = world; this.startAt = startAt; }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            return core == null ? amount * CHARGE_GUARD : amount;                 // l'etoile en charge encaisse
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            long t = now - startAt;
            if (core == null) {
                Vector3d pos = SpellFx.position(caster, store);
                if (pos == null) return true;                                     // mort pendant la charge : pas d'explosion
                if (t >= CHARGE_MS) { explode(caster, pos, store, cb); return false; }
                charge(caster, pos, t, now, store, cb);
                return false;
            }
            double r = CORE + WAVE_SPEED * (t - CHARGE_MS) / 1000.0;
            if (r > WAVE_END + WAVE_WIDTH) return true;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, core, Math.min(r, WAVE_END) + HEIGHT, store)) {
                if (hit.contains(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                double d = SpellFx.horizontalDistance(p, core);
                if (d > r || d < r - WAVE_WIDTH || d > WAVE_END || Math.abs(p.y - core.y) > HEIGHT) continue;
                hit.add(e);
                Vector3d out = new Vector3d(p.x - core.x, 0, p.z - core.z).normalize().mul(WAVE_PUSH).add(0, 4, 0);
                SpellFx.damage(caster, e, "Fire", WAVE_DAMAGE, out, "Impact_Strong", cb);
            }
            return false;
        }

        /** Charge : l'etoile attire et consume, l'anneau de feu se resserre. */
        private void charge(Ref<EntityStore> caster, Vector3d pos, long t, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            boolean burn = now - lastBurn >= BURN_EVERY_MS;
            if (burn) lastBurn = now;
            if (now - lastPull >= PULL_EVERY_MS) {
                lastPull = now;
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, PULL_RADIUS, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || RuneSpellRuntime.isSummon(e)) continue;
                    Vector3d in = new Vector3d(pos.x - p.x, 0, pos.z - p.z);
                    if (in.lengthSquared() > 4.0) SpellFx.push(e, in.normalize().mul(PULL), true, cb);
                    if (burn) SpellFx.damage(caster, e, "Fire", CHARGE_BURN, null, null, cb);
                }
                double ringR = PULL_RADIUS - (PULL_RADIUS - 2) * Math.min(1.0, t / (double) CHARGE_MS);
                for (int i = 0; i < RING_POINTS; i++) {
                    double a = i * 2 * Math.PI / RING_POINTS + t / 400.0;
                    SpellFx.fx(FX_FIRE, new Vector3d(pos).add(Math.cos(a) * ringR, 0.5, Math.sin(a) * ringR), 0.8f, store, cb);
                }
            }
            if (now - lastPillar >= PILLAR_EVERY_MS) { lastPillar = now; SpellFx.fx(FX_PILLAR, pos, 0.8f, store, cb); }
        }

        /** L'etoile meurt : coeur devastateur, onde de choc, sol calcine. */
        private void explode(Ref<EntityStore> caster, Vector3d pos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            core = new Vector3d(pos);
            try { EffectsManager.removeEffect(caster, CHANNEL, cb); } catch (Exception e) { EldaniorLogger.error("Supernova: canalisation", e); }
            Vector3d mid = new Vector3d(core).add(0, 1, 0);
            SpellFx.fx(FX_FLASH, mid, 10.0f, store, cb);
            SpellFx.fx(FX_EXPLOSION, mid, 5.0f, store, cb);
            SpellFx.fx(FX_BLAST, mid, 2.0f, store, cb);
            SpellFx.fx(FX_SHOCKWAVE, new Vector3d(core).add(0, 0.2, 0), (float) (WAVE_END / 2.0), store, cb);
            SpellFx.sound(SFX_BOOM, core, cb);
            SpellFx.shake(core, WAVE_END + 30, "Impact_Strong", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, core, CORE + HEIGHT, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, core) > CORE || Math.abs(p.y - core.y) > HEIGHT) continue;
                hit.add(e);
                Vector3d out = new Vector3d(p.x - core.x, 0, p.z - core.z);
                if (out.lengthSquared() < 1e-4) out.set(1, 0, 0);
                SpellFx.damage(caster, e, "Fire", CORE_DAMAGE, out.normalize().mul(CORE_PUSH).add(0, 6, 0), "Impact_Strong", cb);
                SpellFx.effect(e, BURN, cb);
            }
            scorch();
        }

        /** Surface calcinee autour du coeur, restauree apres 60 s. */
        private void scorch() {
            TerrainRestore.Restore batch = TerrainRestore.open(world, System.currentTimeMillis() + SCORCH_MS);
            int cx = (int) Math.floor(core.x), cy = (int) Math.floor(core.y), cz = (int) Math.floor(core.z), r = (int) SCORCH_RADIUS;
            world.execute(() -> {
                try {
                    String w = world.getName();
                    for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
                        if (Math.hypot(dx, dz) > SCORCH_RADIUS) continue;
                        int x = cx + dx, z = cz + dz;
                        for (int y = cy + 2; y >= cy - 4; y--) {
                            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z), above = WorldBlockUtil.getBlockType(world, x, y + 1, z);
                            if (t == null || t.getMaterial() != BlockMaterial.Solid || above == null || above.getMaterial() == BlockMaterial.Solid) continue;
                            String id = t.getId();
                            if (!id.equals(SCORCHED) && !id.startsWith("Plant_") && !TerrainRestore.isKept(world, id, x, y, z) && !TerrainRestore.isProtectedArea(w, x, y, z)
                                    && WorldBlockUtil.getFluidIndex(world, x, y + 1, z) <= 0)
                                TerrainRestore.place(world, batch, x, y, z, id, SCORCHED, 0);
                            break;
                        }
                    }
                    TerrainRestore.save();
                } catch (Exception e) {
                    EldaniorLogger.error("Supernova: sol calcine", e);
                }
            });
        }
    }
}
