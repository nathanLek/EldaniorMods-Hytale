package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de Boule de Feu (Feu, Rare ; Mage et toutes ses sous-classes, Dragon) : grosse sphere de flammes, plus lente que la
 * Flammeche, qui EXPLOSE au premier contact (ennemi, mur, sol) ou au bout de sa course : degats de zone, projection vers
 * l'exterieur et brulure. La Flammeche, elle, ne touche qu'une cible. Terrain intact.
 * Le projectile est un modele porteur (Projectile_Config_BouleDeFeu, modele Fireball agrandi) deplace par le code.
 * Fiche de conception : scripts/runes/specs/Rune_Fireball.json
 */
public final class BouleDeFeu implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double SPEED = 20.0, RANGE = 35.0, HIT_RADIUS = 1.4, STEP = 0.5;
    private static final double CORE = 2.0, BLAST = 4.0;
    private static final float CORE_DAMAGE = 180f, BLAST_DAMAGE = 110f;
    private static final float MODEL_SCALE = 2.2f;
    private static final long MAX_MS = 2_500;

    // ===== Assets =====
    private static final String CARRIER = "Projectile_Config_BouleDeFeu", MODEL = "Fireball";
    private static final String BURN = "Elda_Brulure";
    private static final String FX_EXPLOSION = "Explosion_Medium", FX_IMPACT = "Impact_Fire";
    private static final String SFX_EXPLOSION = "SFX_Fireball_Death", SFX_IMPACT = "SFX_Staff_Flame_Fireball_Impact";

    @Override public String id() { return "BouleDeFeu"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d dir = SpellFx.lookDirection(caster, store);
        if (dir == null) return null;
        Vector3d from = new Vector3d(pos).add(0, 1.5, 0).add(new Vector3d(dir).mul(1.0));
        Ref<EntityStore> model = SpellFx.spawnModel(caster, CARRIER, from, dir, cb);
        SpellFx.notSaved(model, store, cb);
        return new Ball(from, new Vector3d(dir).normalize(), model, System.currentTimeMillis());
    }

    private static final class Ball implements Instance {
        final Vector3d pos, dir;
        final Ref<EntityStore> model;
        final long startAt;
        long last;
        double travelled;
        boolean scaled;

        Ball(Vector3d pos, Vector3d dir, Ref<EntityStore> model, long startAt) {
            this.pos = pos; this.dir = dir; this.model = model; this.startAt = startAt; this.last = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!scaled && model != null && model.isValid()) {                         // modele cree au tick suivant
                scaled = true;
                SpellFx.setModelScale(model, MODEL, MODEL_SCALE, 0.25, cb);
            }
            if (now - startAt >= MAX_MS || travelled >= RANGE) { explode(caster, store, cb); return true; }
            World world = SpellFx.world(store);
            double move = SPEED * Math.min(0.2, (now - last) / 1000.0);
            last = now;
            // Avance par petits pas pour ne traverser ni un mur ni un ennemi
            for (double done = 0; done < move; done += STEP) {
                double step = Math.min(STEP, move - done);
                pos.add(new Vector3d(dir).mul(step));
                int x = (int) Math.floor(pos.x), y = (int) Math.floor(pos.y), z = (int) Math.floor(pos.z);
                if (WorldBlockUtil.getBlockType(world, x, y, z) == null || !TerrainRestore.isEmpty(world, x, y, z)) {
                    pos.sub(new Vector3d(dir).mul(step));                               // explose devant le mur, pas dedans
                    explode(caster, store, cb);
                    return true;
                }
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, HIT_RADIUS + 1, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || p.add(0, 1, 0).distance(pos) > HIT_RADIUS) continue;
                    explode(caster, store, cb);
                    return true;
                }
            }
            travelled += move;
            SpellFx.hold(model, pos, SpellFx.facing(dir, true), store);
            return false;
        }

        /** Explosion : coeur (CORE) et souffle (BLAST), projection vers l'exterieur, brulure. */
        private void explode(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.fx(FX_EXPLOSION, pos, 1.3f, store, cb);
            SpellFx.fx(FX_IMPACT, pos, 1.6f, store, cb);
            SpellFx.sound(SFX_EXPLOSION, pos, cb);
            SpellFx.sound(SFX_IMPACT, pos, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, BLAST + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d body = new Vector3d(p).add(0, 1, 0);
                double d = body.distance(pos);
                if (d > BLAST) continue;
                Vector3d push = new Vector3d(body).sub(pos);
                push.y = 0;
                if (push.lengthSquared() < 1e-4) push.set(dir.x, 0, dir.z);
                if (push.lengthSquared() > 1e-4) push.normalize();
                double force = d <= CORE ? 9 : 6;
                SpellFx.damage(caster, e, "Fire", d <= CORE ? CORE_DAMAGE : BLAST_DAMAGE,
                        push.mul(force).add(0, d <= CORE ? 5 : 3.5, 0), "Impact_Light", cb);
                SpellFx.effect(e, BURN, cb);
            }
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
