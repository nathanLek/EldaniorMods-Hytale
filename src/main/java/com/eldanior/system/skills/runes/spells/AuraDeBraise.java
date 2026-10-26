package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de l'Aura de Braise (Feu, Rare) : aura de feu de 6 s autour du lanceur qui brule les ennemis proches et laisse
 * une trainee de sol calcine (herbe et terre seulement, 50 blocs max, restaure apres 1 minute). Lanceur immunise au feu.
 * Fiche de conception : scripts/runes/specs/Rune_AuraDeBraise.json
 */
public final class AuraDeBraise implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, DAMAGE_EVERY_MS = 1_000, FX_EVERY_MS = 500, GROUND_EVERY_MS = 200;
    private static final long RESTORE_AFTER_MS = 60_000;
    private static final double RADIUS = 4.0, MAX_HEIGHT = 3.0;
    private static final float DAMAGE = 15f;
    private static final int MAX_BLOCKS = 50;

    // ===== Assets =====
    private static final String AURA = "Elda_AuraDeBraise", BURN = "Elda_Brulure";
    private static final String BURNT = "Soil_Dirt_Burnt";
    private static final String FX_SPAWN = "Fire_AoE_Spawn", FX_FLAME = "Impact_Fire";
    private static final String SFX_IGNITE = "SFX_Flame_Ignite", SFX_BURN = "SFX_Effect_Burn_World";

    @Override public String id() { return "AuraDeBraise"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        long now = System.currentTimeMillis();
        SpellFx.effect(caster, AURA, cb);
        SpellFx.fx(FX_SPAWN, new Vector3d(pos).add(0, 0.1, 0), 0.6f, store, cb);
        SpellFx.sound(SFX_IGNITE, pos, cb);
        World world = SpellFx.world(store);
        return new Aura(caster, world, now);
    }

    private static final class Aura implements Instance {
        final Ref<EntityStore> caster;
        final World world;
        final long startAt;
        final TerrainRestore.Restore terrain;
        long lastDamage, lastFx, lastGround;
        int placed;

        Aura(Ref<EntityStore> caster, World world, long startAt) {
            this.caster = caster; this.world = world; this.startAt = startAt;
            this.lastDamage = startAt - DAMAGE_EVERY_MS;
            this.terrain = TerrainRestore.open(world, startAt + DURATION_MS + RESTORE_AFTER_MS);
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            if (now - lastFx >= FX_EVERY_MS) {
                lastFx = now;
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                for (int i = 0; i < 3; i++) {
                    double a = rng.nextDouble() * Math.PI * 2, r = 1 + rng.nextDouble() * (RADIUS - 1);
                    SpellFx.fx(FX_FLAME, new Vector3d(pos).add(Math.cos(a) * r, 0.3, Math.sin(a) * r), 0.7f, store, cb);
                }
            }
            if (now - lastDamage >= DAMAGE_EVERY_MS) {
                lastDamage = now;
                SpellFx.sound(SFX_BURN, pos, cb);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, RADIUS, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || Math.abs(p.y - pos.y) > MAX_HEIGHT) continue;
                    SpellFx.damage(caster, e, "Fire", DAMAGE, null, null, cb);
                    SpellFx.effect(e, BURN, cb);
                }
            }
            if (placed < MAX_BLOCKS && now - lastGround >= GROUND_EVERY_MS) {
                lastGround = now;
                scorch((int) Math.floor(pos.x), (int) Math.floor(pos.y) - 1, (int) Math.floor(pos.z));
            }
            return false;
        }

        /** Calcine le bloc sous les pieds (herbe ou terre uniquement, hors zones protegees). */
        private void scorch(int x, int y, int z) {
            placed++;                                                         // reserve la place : l'ecriture est differee
            world.execute(() -> {
                try {
                    if (TerrainRestore.isProtectedArea(world.getName(), x, y, z)) return;
                    BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                    if (t == null || TerrainRestore.isKept(world, t.getId(), x, y, z)) return;
                    String id = t.getId();
                    if (!id.equals("Soil_Grass") && !id.equals("Soil_Dirt")) return;
                    if (WorldBlockUtil.getFluidIndex(world, x, y + 1, z) > 0) return;
                    TerrainRestore.place(world, terrain, x, y, z, id, BURNT, 0);
                } catch (Exception e) {
                    EldaniorLogger.error("AuraDeBraise: sol calcine", e);
                }
            });
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (caster.isValid()) {
                try { EffectsManager.removeEffect(caster, AURA, cb); } catch (Exception e) { EldaniorLogger.error("AuraDeBraise: effet", e); }
            }
            TerrainRestore.save();
        }

        @Override
        public void abort() {
            TerrainRestore.save();
        }
    }
}
