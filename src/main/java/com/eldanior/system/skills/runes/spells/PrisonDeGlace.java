package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de la Prison de Glace (Glace, Rare) : enferme l'ennemi vise dans une cage de vrais blocs de glace (cases vides
 * seulement, restaurees a l'eclatement) et le gele 1,5 s ; puis la prison eclate et blesse la cible et ses voisins.
 * Fiche de conception : scripts/runes/specs/Rune_PrisonDeGlace.json
 */
public final class PrisonDeGlace implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long PRISON_MS = 1_500;
    private static final double TARGET_RANGE = 25.0, SPLASH_RADIUS = 3.0;
    private static final float SHATTER_DAMAGE = 100f, SPLASH_DAMAGE = 40f;

    // ===== Assets =====
    private static final String ICE = "Rock_Ice";
    private static final String FREEZE = "Elda_Gel", SLOW = "Elda_Givre";
    private static final String FX_FORM = "Effect_Snow_Impact", FX_SHATTER = "Ability_Ice_Explosion", FX_SHARDS = "Block_Break_Ice";
    private static final String SFX_FORM = "SFX_Ice_Build", SFX_SHATTER = "SFX_Ice_Break";

    @Override public String id() { return "PrisonDeGlace"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> target = SpellFx.targetEntity(caster, store, TARGET_RANGE);
        Vector3d at = SpellFx.position(target, store);
        if (at == null) return null;
        long now = System.currentTimeMillis();
        World world = SpellFx.world(store);
        TerrainRestore.Restore cage = TerrainRestore.open(world, now + PRISON_MS);
        build(world, cage, (int) Math.floor(at.x), (int) Math.floor(at.y), (int) Math.floor(at.z));

        EffectsManager.applyCustomEffect(target, FREEZE, PRISON_MS / 1000f, OverlapBehavior.OVERWRITE, cb);
        SpellFx.lockInPlace(target, PRISON_MS, store);
        SpellFx.fx(FX_FORM, new Vector3d(at).add(0, 1, 0), 1.2f, store, cb);
        SpellFx.sound(SFX_FORM, at, cb);
        return new Prison(target, at, world, cage, now);
    }

    /** Anneau de 8 cases sur 2 de haut autour de la cible, plus un couvercle : seules les cases vides recoivent la glace. */
    private static void build(World world, TerrainRestore.Restore cage, int cx, int cy, int cz) {
        world.execute(() -> {
            try {
                for (int dy = 0; dy <= 2; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            boolean wall = dx != 0 || dz != 0;
                            if (dy < 2 ? !wall : wall) continue;                     // murs sur 2 niveaux, couvercle au centre
                            int x = cx + dx, y = cy + dy, z = cz + dz;
                            if (!TerrainRestore.isEmpty(world, x, y, z) || TerrainRestore.isProtectedArea(world.getName(), x, y, z)) continue;
                            if (WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) continue;
                            TerrainRestore.place(world, cage, x, y, z, TerrainRestore.EMPTY, ICE, 0);
                        }
                    }
                }
            } catch (Exception e) {
                EldaniorLogger.error("PrisonDeGlace: cage", e);
            }
        });
    }

    private static final class Prison implements Instance {
        final Ref<EntityStore> target;
        final Vector3d at;
        final World world;
        final TerrainRestore.Restore cage;
        final long startAt;

        Prison(Ref<EntityStore> target, Vector3d at, World world, TerrainRestore.Restore cage, long startAt) {
            this.target = target; this.at = at; this.world = world; this.cage = cage; this.startAt = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            boolean dead = !target.isValid() || store.getComponent(target, DeathComponent.getComponentType()) != null;
            if (!dead && now - startAt < PRISON_MS) return false;
            shatter(caster, dead, store, cb);
            return true;
        }

        private void shatter(Ref<EntityStore> caster, boolean dead, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d mid = new Vector3d(at).add(0, 1, 0);
            SpellFx.fx(FX_SHATTER, mid, 1.0f, store, cb);
            SpellFx.fx(FX_SHARDS, mid, 1.2f, store, cb);
            SpellFx.sound(SFX_SHATTER, at, cb);
            if (!dead) {
                SpellFx.damage(caster, target, "Ice", SHATTER_DAMAGE, null, "Impact_Light", cb);
                SpellFx.effect(target, SLOW, cb);
            }
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, SPLASH_RADIUS, store)) {
                if (e.equals(target)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d out = new Vector3d(p.x - at.x, 0, p.z - at.z);
                if (out.lengthSquared() > 0.25) out.normalize().mul(5);
                SpellFx.damage(caster, e, "Ice", SPLASH_DAMAGE, out.add(0, 3, 0), null, cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (target.isValid()) {
                SpellFx.unlock(target);
                try { EffectsManager.removeEffect(target, FREEZE, cb); } catch (Exception e) { EldaniorLogger.error("PrisonDeGlace: effet", e); }
            }
            world.execute(() -> TerrainRestore.restoreNow(world, cage));
        }

        @Override
        public void abort() {
            SpellFx.unlock(target);
            TerrainRestore.save();                                            // la cage se restaure seule a sa date (1,5 s)
        }
    }
}
