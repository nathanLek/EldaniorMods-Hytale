package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

/**
 * Rune de la Lance de Cristal (Glace, Rare) : lance de glace qui traverse tous les ennemis alignes ; degats doubles sur
 * les cibles gelees (Elda_Gel, Niflheim_Gel, Freeze). Modele porteur Ice_Bolt agrandi, deplace par le code.
 * Fiche de conception : scripts/runes/specs/Rune_LanceDeCristal.json
 */
public final class LanceDeCristal implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double SPEED = 45.0, RANGE = 30.0, HIT_RADIUS = 1.3, STEP = 0.5;
    private static final float DAMAGE = 120f, FROZEN_MULTIPLIER = 2f;
    private static final float MODEL_SCALE = 1.8f;
    private static final long MAX_MS = 2_000;

    // ===== Assets =====
    private static final String CARRIER = "Projectile_Config_LanceDeCristal", MODEL = "Ice_Bolt";
    private static final String SLOW = "Elda_Givre";
    private static final String[] FROZEN = {"Elda_Gel", "Niflheim_Gel", "Freeze"};
    private static final String FX_HIT = "Impact_Ice", FX_SHATTER = "Ability_Ice_Explosion", FX_WALL = "Block_Break_Ice";
    private static final String SFX_HIT = "SFX_Ice_Item_Impact", SFX_SHATTER = "SFX_Ice_Break";

    @Override public String id() { return "LanceDeCristal"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d dir = SpellFx.lookDirection(caster, store);
        Vector3d from = new Vector3d(pos).add(0, 1.5, 0).add(new Vector3d(dir).mul(1.0));
        Ref<EntityStore> model = SpellFx.spawnModel(caster, CARRIER, from, dir, cb);
        SpellFx.notSaved(model, store, cb);
        return new Lance(from, new Vector3d(dir).normalize(), model, System.currentTimeMillis());
    }

    private static boolean frozen(Ref<EntityStore> e, Store<EntityStore> store) {
        for (String id : FROZEN) if (EffectsManager.hasEffect(e, id, store)) return true;
        return false;
    }

    private static final class Lance implements Instance {
        final Vector3d pos, dir;
        final Ref<EntityStore> model;
        final long startAt;
        final Set<Ref<EntityStore>> hit = new HashSet<>();
        long last;
        double travelled;
        boolean scaled;

        Lance(Vector3d pos, Vector3d dir, Ref<EntityStore> model, long startAt) {
            this.pos = pos; this.dir = dir; this.model = model; this.startAt = startAt; this.last = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= MAX_MS || travelled >= RANGE) return true;
            if (!scaled && model != null && model.isValid()) {                   // le porteur n'existe qu'au tick suivant sa creation
                SpellFx.setModelScale(model, MODEL, MODEL_SCALE, 0.8, cb);
                scaled = true;
            }
            World world = SpellFx.world(store);
            double move = SPEED * Math.min(0.2, (now - last) / 1000.0);
            last = now;
            for (double done = 0; done < move; done += STEP) {
                pos.add(new Vector3d(dir).mul(Math.min(STEP, move - done)));
                int x = (int) Math.floor(pos.x), y = (int) Math.floor(pos.y), z = (int) Math.floor(pos.z);
                if (WorldBlockUtil.getBlockType(world, x, y, z) == null || !TerrainRestore.isEmpty(world, x, y, z)) {
                    SpellFx.fx(FX_WALL, pos, 1.0f, store, cb);
                    SpellFx.sound(SFX_SHATTER, pos, cb);
                    return true;
                }
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, HIT_RADIUS + 1, store)) {
                    if (hit.contains(e)) continue;
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || p.add(0, 1, 0).distance(pos) > HIT_RADIUS) continue;
                    hit.add(e);
                    pierce(e, p, caster, store, cb);
                }
            }
            travelled += move;
            SpellFx.hold(model, pos, SpellFx.facing(dir, true), store);
            return false;
        }

        /** Traverse la cible sans s'arreter ; une cible gelee vole en eclats (degats doubles). */
        private void pierce(Ref<EntityStore> e, Vector3d at, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            boolean shatter = frozen(e, store);
            SpellFx.damage(caster, e, "Ice", shatter ? DAMAGE * FROZEN_MULTIPLIER : DAMAGE, new Vector3d(dir).mul(3).add(0, 1.5, 0), "Impact_Light", cb);
            SpellFx.effect(e, SLOW, cb);
            SpellFx.fx(shatter ? FX_SHATTER : FX_HIT, at, shatter ? 1.0f : 0.8f, store, cb);
            SpellFx.sound(shatter ? SFX_SHATTER : SFX_HIT, at, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(model, cb);
        }

        @Override
        public void abort() {
            RuneSpellRuntime.removeLater(model);
        }
    }
}
