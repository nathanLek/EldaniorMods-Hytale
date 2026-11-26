package com.eldanior.system.skills.runes.spells;

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
 * Amaterasu et Tsukuyomi, Lames Jumelles (artefact legendaire, Artefact_Sword_Katanas_Jumeaux). Arme, croix d'energie
 * (modele Katanas_Croix : une lame d'or, une lame de glace, croisees) et animations : scripts/artefacts/gen_katanas_jumeaux.py.
 * La croix est un modele porte par Projectile_Config_Katanas_Croix, deplace par le code (comme la Lance de Cristal) ; elle
 * traverse les ennemis (une touche chacun : degats, embrasement, givre) et s'arrete au premier mur.
 *   - CroixJumelle (attaque maintenue, interaction Katanas_Jumeaux_Croix) : croix rapide, a distance ;
 *   - EclipseJumelle (signature, interaction Katanas_Jumeaux_Signature) : croix geante, plus lente, qui explose en feu et
 *     en glace en fin de course (ou contre un mur).
 */
public final class LamesJumelles {

    private LamesJumelles() {}

    // ===== Assets communs =====
    private static final String CARRIER = "Projectile_Config_Katanas_Croix", MODEL = "Katanas_Croix";
    private static final String BURN = "Arme_Embrasement", FROST = "Arme_Givre";
    private static final String FX_FIRE = "Impact_Fire", FX_ICE = "Ability_Ice_Impact";
    private static final String FX_EXPLOSION = "Explosion_Big", FX_FREEZE = "Niflheim_Freeze_Burst";
    private static final String SFX_HIT = "SFX_Sword_T2_Impact", SFX_FIRE = "SFX_Flame_Ignite", SFX_ICE = "SFX_Ice_Break";

    /** Reglages d'une croix : vitesse et portee (blocs), rayon de touche, degats, echelle du modele, explosion finale. */
    private record Croix(double speed, double range, double hitRadius, float damage, float scale,
                         double blastRadius, float blastDamage) {}

    public static final class CroixJumelle implements RuneSpell {
        private static final Croix CROIX = new Croix(24.0, 12.0, 1.4, 90f, 1.0f, 0, 0);

        @Override public String id() { return "CroixJumelle"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            return Flight.launch(caster, CROIX, store, cb);
        }
    }

    public static final class EclipseJumelle implements RuneSpell {
        private static final Croix CROIX = new Croix(14.0, 14.0, 2.4, 150f, 2.2f, 5.0, 220f);

        @Override public String id() { return "EclipseJumelle"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            return Flight.launch(caster, CROIX, store, cb);
        }
    }

    private static final class Flight implements RuneSpell.Instance {
        private static final double STEP = 0.5;
        private static final long MAX_MS = 3_000;

        final Croix croix;
        final Vector3d pos, dir;
        final Ref<EntityStore> model;
        final long startAt;
        final Set<Ref<EntityStore>> hit = new HashSet<>();
        long last;
        double travelled;
        boolean scaled;

        private Flight(Croix croix, Vector3d pos, Vector3d dir, Ref<EntityStore> model, long startAt) {
            this.croix = croix; this.pos = pos; this.dir = dir; this.model = model; this.startAt = startAt; this.last = startAt;
        }

        static Flight launch(Ref<EntityStore> caster, Croix croix, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d at = SpellFx.position(caster, store);
            if (at == null) return null;
            Vector3d dir = SpellFx.lookDirection(caster, store).normalize();
            Vector3d from = new Vector3d(at).add(0, 1.3, 0).add(new Vector3d(dir).mul(1.0));
            Ref<EntityStore> model = SpellFx.spawnModel(caster, CARRIER, from, dir, cb);
            SpellFx.notSaved(model, store, cb);
            SpellFx.sound(SFX_FIRE, from, cb);
            SpellFx.sound(SFX_ICE, from, cb);
            return new Flight(croix, from, new Vector3d(dir), model, System.currentTimeMillis());
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= MAX_MS || travelled >= croix.range()) { explode(caster, store, cb); return true; }
            if (!scaled && model != null && model.isValid()) {                   // le porteur n'existe qu'au tick suivant sa creation
                if (croix.scale() != 1f) SpellFx.setModelScale(model, MODEL, croix.scale(), 1.2 * croix.scale(), cb);
                scaled = true;
            }
            World world = SpellFx.world(store);
            double move = croix.speed() * Math.min(0.2, (now - last) / 1000.0);
            last = now;
            for (double done = 0; done < move; done += STEP) {
                pos.add(new Vector3d(dir).mul(Math.min(STEP, move - done)));
                int x = (int) Math.floor(pos.x), y = (int) Math.floor(pos.y), z = (int) Math.floor(pos.z);
                if (WorldBlockUtil.getBlockType(world, x, y, z) == null || !TerrainRestore.isEmpty(world, x, y, z)) {
                    explode(caster, store, cb);                                  // un mur arrete la croix
                    return true;
                }
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, croix.hitRadius() + 1, store)) {
                    if (hit.contains(e)) continue;
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || p.add(0, 1, 0).distance(pos) > croix.hitRadius()) continue;
                    hit.add(e);
                    strike(caster, e, croix.damage(), p, store, cb);
                }
            }
            travelled += move;
            SpellFx.hold(model, pos, SpellFx.facing(dir, true), store);
            return false;
        }

        private void strike(Ref<EntityStore> caster, Ref<EntityStore> e, float damage, Vector3d at, Store<EntityStore> store,
                            CommandBuffer<EntityStore> cb) {
            SpellFx.damage(caster, e, "Physical", damage, new Vector3d(dir).mul(4).add(0, 2, 0), "Impact_Light", cb);
            SpellFx.effect(e, BURN, cb);
            SpellFx.effect(e, FROST, cb);
            SpellFx.fx(FX_FIRE, at, 0.8f, store, cb);
            SpellFx.fx(FX_ICE, at, 0.6f, store, cb);
            SpellFx.sound(SFX_HIT, at, cb);
        }

        /** Fin de course de la croix d'Eclipse : explosion de feu et de glace (rien pour la Croix Jumelle). */
        private void explode(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (croix.blastRadius() <= 0 || caster == null || !caster.isValid()) return;
            SpellFx.fx(FX_EXPLOSION, pos, 1.2f, store, cb);
            SpellFx.fx(FX_FREEZE, pos, 1.5f, store, cb);
            SpellFx.sound(SFX_FIRE, pos, cb);
            SpellFx.sound(SFX_ICE, pos, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, croix.blastRadius() + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || p.add(0, 1, 0).distance(pos) > croix.blastRadius()) continue;
                Vector3d out = new Vector3d(p).sub(pos);
                out = out.lengthSquared() > 0.25 ? out.normalize().mul(8) : new Vector3d(dir).mul(8);
                SpellFx.damage(caster, e, "Physical", croix.blastDamage(), out.add(0, 4, 0), "Impact_Strong", cb);
                SpellFx.effect(e, BURN, cb);
                SpellFx.effect(e, FROST, cb);
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
