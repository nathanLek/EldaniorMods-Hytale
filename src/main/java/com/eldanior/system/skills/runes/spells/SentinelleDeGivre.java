package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de la Sentinelle de Givre (Glace, Rare) : tourelle de vrais blocs de glace (3 de haut + pointe, cases vides
 * seulement) au point vise pendant 12 s ; chaque seconde, elle tire un eclat ralentissant sur l'ennemi le plus proche.
 * Les blocs sont restaures a la fin.
 * Fiche de conception : scripts/runes/specs/Rune_SentinelleDeGivre.json
 */
public final class SentinelleDeGivre implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 12_000, SHOT_EVERY_MS = 1_000;
    private static final double TARGET_RANGE = 20.0, FALLBACK = 6.0, SHOT_RANGE = 14.0;
    private static final int HEIGHT = 3;
    private static final float DAMAGE = 22f;

    // ===== Assets =====
    private static final String ICE = "Rock_Ice", TIP = "Rock_Ice_Icicles";
    private static final String SLOW = "Elda_Givre";
    private static final String FX_SHOT = "Impact_Ice", FX_HIT = "Ability_Ice_Impact", FX_MELT = "Block_Break_Ice";
    private static final String SFX_BUILD = "SFX_Ice_Build", SFX_SHOT = "SFX_Arrow_Frost_Hit", SFX_MELT = "SFX_Ice_Break";

    @Override public String id() { return "SentinelleDeGivre"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        World world = SpellFx.world(store);
        Vector3d base = SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK);
        long now = System.currentTimeMillis();
        TerrainRestore.Restore tower = TerrainRestore.open(world, now + DURATION_MS);
        int x = (int) Math.floor(base.x), y = (int) Math.floor(base.y), z = (int) Math.floor(base.z);
        world.execute(() -> {
            try {
                for (int dy = 0; dy <= HEIGHT; dy++) {
                    if (!TerrainRestore.isEmpty(world, x, y + dy, z) || TerrainRestore.isProtectedArea(world.getName(), x, y + dy, z)) break;
                    TerrainRestore.place(world, tower, x, y + dy, z, TerrainRestore.EMPTY, dy < HEIGHT ? ICE : TIP, 0);
                }
                TerrainRestore.save();
            } catch (Exception e) {
                EldaniorLogger.error("SentinelleDeGivre: tourelle", e);
            }
        });
        SpellFx.sound(SFX_BUILD, base, cb);
        return new Turret(new Vector3d(x + 0.5, y + HEIGHT + 0.3, z + 0.5), world, tower, now);
    }

    private static final class Turret implements Instance {
        final Vector3d muzzle;
        final World world;
        final TerrainRestore.Restore tower;
        final long startAt;
        long lastShot;

        Turret(Vector3d muzzle, World world, TerrainRestore.Restore tower, long startAt) {
            this.muzzle = muzzle; this.world = world; this.tower = tower; this.startAt = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastShot < SHOT_EVERY_MS) return false;
            Ref<EntityStore> target = null;
            double best = SHOT_RANGE * SHOT_RANGE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, muzzle, SHOT_RANGE, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                double d = p.distanceSquared(muzzle);
                if (d < best) { best = d; target = e; }
            }
            if (target == null) return false;
            lastShot = now;
            Vector3d tp = SpellFx.position(target, store).add(0, 1, 0);
            SpellFx.arc(FX_SHOT, muzzle, tp, 0.5f, store, cb);
            SpellFx.fx(FX_HIT, tp, 0.8f, store, cb);
            SpellFx.sound(SFX_SHOT, muzzle, cb);
            SpellFx.damage(caster, target, "Ice", DAMAGE, null, null, cb);
            SpellFx.effect(target, SLOW, cb);
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            Vector3d mid = new Vector3d(muzzle).sub(0, 1.5, 0);
            try { SpellFx.fx(FX_MELT, mid, 1.2f, world.getEntityStore().getStore(), cb); } catch (Exception e) { EldaniorLogger.error("SentinelleDeGivre: fonte", e); }
            SpellFx.sound(SFX_MELT, mid, cb);
            world.execute(() -> TerrainRestore.restoreNow(world, tower));
        }

        @Override
        public void abort() {
            TerrainRestore.save();
        }
    }
}
