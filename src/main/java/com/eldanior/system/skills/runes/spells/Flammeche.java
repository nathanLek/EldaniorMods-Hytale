package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de Flammèche (Feu, Common) : petite boule de feu rapide qui touche le premier ennemi sur sa trajectoire et l'enflamme.
 * Le projectile est un modele porteur (Projectile_Config_Flammeche) deplace par le code : la trajectoire, les collisions
 * et les degats sont calcules ici (allies epargnes).
 * Fiche de conception : scripts/runes/specs/Rune_Flammeche.json
 */
public final class Flammeche implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double SPEED = 35.0, RANGE = 30.0, HIT_RADIUS = 1.2, STEP = 0.5;
    private static final float DAMAGE = 50f;
    private static final long MAX_MS = 2_000;

    // ===== Assets =====
    private static final String CARRIER = "Projectile_Config_Flammeche";
    private static final String BURN = "Elda_Brulure";
    private static final String FX_IMPACT = "Impact_Fire";
    private static final String SFX_IMPACT = "SFX_Fireball_Death";

    @Override public String id() { return "Flammeche"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d dir = SpellFx.lookDirection(caster, store);
        if (dir == null) return null;
        Vector3d from = new Vector3d(pos).add(0, 1.5, 0).add(new Vector3d(dir).mul(0.8));
        Ref<EntityStore> model = SpellFx.spawnModel(caster, CARRIER, from, dir, cb);
        SpellFx.notSaved(model, store, cb);
        return new Bolt(from, new Vector3d(dir).normalize(), model, System.currentTimeMillis());
    }

    private static final class Bolt implements Instance {
        final Vector3d pos, dir;
        final Ref<EntityStore> model;
        final long startAt;
        long last;
        double travelled;

        Bolt(Vector3d pos, Vector3d dir, Ref<EntityStore> model, long startAt) {
            this.pos = pos; this.dir = dir; this.model = model; this.startAt = startAt; this.last = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= MAX_MS || travelled >= RANGE) return true;
            World world = SpellFx.world(store);
            double move = SPEED * Math.min(0.2, (now - last) / 1000.0);
            last = now;
            // Avance par petits pas pour ne traverser ni un mur ni un ennemi a grande vitesse
            for (double done = 0; done < move; done += STEP) {
                pos.add(new Vector3d(dir).mul(Math.min(STEP, move - done)));
                int x = (int) Math.floor(pos.x), y = (int) Math.floor(pos.y), z = (int) Math.floor(pos.z);
                if (WorldBlockUtil.getBlockType(world, x, y, z) == null || !TerrainRestore.isEmpty(world, x, y, z)) {
                    impact(null, caster, store, cb);
                    return true;
                }
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, HIT_RADIUS + 1, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || p.add(0, 1, 0).distance(pos) > HIT_RADIUS) continue;    // centre du corps, ~1 bloc au-dessus des pieds
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
            SpellFx.damage(caster, target, "Fire", DAMAGE, new Vector3d(dir).mul(4).add(0, 2, 0), "Impact_Light", cb);
            SpellFx.effect(target, BURN, cb);
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
