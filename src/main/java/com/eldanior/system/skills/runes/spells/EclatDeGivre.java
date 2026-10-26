package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune d'Éclat de Givre (Glace, Common) : boule de givre rapide qui touche le premier ennemi et le ralentit.
 * Meme moteur que la Flammeche : modele porteur (Projectile_Config_EclatDeGivre) deplace par le code.
 * Fiche de conception : scripts/runes/specs/Rune_EclatDeGivre.json
 */
public final class EclatDeGivre implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double SPEED = 40.0, RANGE = 30.0, HIT_RADIUS = 1.2, STEP = 0.5;
    private static final float DAMAGE = 45f;
    private static final long MAX_MS = 2_000;

    // ===== Assets =====
    private static final String CARRIER = "Projectile_Config_EclatDeGivre";
    private static final String SLOW = "Elda_Givre";
    private static final String FX_IMPACT = "Impact_Ice";
    private static final String SFX_IMPACT = "SFX_Ice_Ball_Death";

    @Override public String id() { return "EclatDeGivre"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d dir = SpellFx.lookDirection(caster, store);
        Vector3d from = new Vector3d(pos).add(0, 1.5, 0).add(new Vector3d(dir).mul(0.8));
        Ref<EntityStore> model = SpellFx.spawnModel(caster, CARRIER, from, dir, cb);
        SpellFx.notSaved(model, store, cb);
        return new Shard(from, new Vector3d(dir).normalize(), model, System.currentTimeMillis());
    }

    private static final class Shard implements Instance {
        final Vector3d pos, dir;
        final Ref<EntityStore> model;
        final long startAt;
        long last;
        double travelled;

        Shard(Vector3d pos, Vector3d dir, Ref<EntityStore> model, long startAt) {
            this.pos = pos; this.dir = dir; this.model = model; this.startAt = startAt; this.last = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= MAX_MS || travelled >= RANGE) return true;
            World world = SpellFx.world(store);
            double move = SPEED * Math.min(0.2, (now - last) / 1000.0);
            last = now;
            for (double done = 0; done < move; done += STEP) {
                pos.add(new Vector3d(dir).mul(Math.min(STEP, move - done)));
                int x = (int) Math.floor(pos.x), y = (int) Math.floor(pos.y), z = (int) Math.floor(pos.z);
                if (WorldBlockUtil.getBlockType(world, x, y, z) == null || !TerrainRestore.isEmpty(world, x, y, z)) {
                    impact(null, caster, store, cb);
                    return true;
                }
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, HIT_RADIUS + 1, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || p.add(0, 1, 0).distance(pos) > HIT_RADIUS) continue;
                    impact(e, caster, store, cb);
                    return true;
                }
            }
            travelled += move;
            SpellFx.hold(model, pos, SpellFx.facing(dir, true), store);
            return false;
        }

        private void impact(Ref<EntityStore> target, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.fx(FX_IMPACT, pos, 1.0f, store, cb);
            SpellFx.sound(SFX_IMPACT, pos, cb);
            if (target == null) return;
            SpellFx.damage(caster, target, "Ice", DAMAGE, new Vector3d(dir).mul(3).add(0, 1.5, 0), "Impact_Light", cb);
            SpellFx.effect(target, SLOW, cb);
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
