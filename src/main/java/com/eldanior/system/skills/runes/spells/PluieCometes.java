package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de la Pluie de Cometes (Feu, Epic, rune de predilection du Createur Cosmique) : 10 s au point vise (rayon 8,
 * 25 blocs). Une comete toutes les 0,6 s (16) : point = un ennemi de la zone au hasard, sinon un point au hasard. Chaque
 * comete (Muspelheim_Meteor reduit x1,5) tombe en biais de 18 blocs en 0,7 s : 70 + Elda_Brulure a 2,5 blocs.
 * Fiche de conception : scripts/runes/specs/Rune_PluieCometes.json
 */
public final class PluieCometes implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, EVERY_MS = 600, FALL_MS = 700, FIRST_MS = 200;
    private static final double RANGE = 25.0, RADIUS = 8.0, BLAST = 2.5, DROP = 18.0, SLANT = 9.0;
    private static final int COMETS = 16;
    private static final float DAMAGE = 70f, SCALE = 1.5f;

    // ===== Assets =====
    private static final String COMET = "Projectile_Config_Muspelheim_Meteor", COMET_MODEL = "Muspelheim_Meteor", BURN = "Elda_Brulure";
    private static final String FX_FIRE = "Impact_Fire", FX_BLAST = "Explosion_Small";
    private static final String SFX_FALL = "SFX_Gobliterator_Rocket_Barrage_Projectile", SFX_IMPACT = "SFX_Gobliterator_Rocket_Barrage_Explosion";

    @Override public String id() { return "PluieCometes"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        World world = SpellFx.world(store);
        if (SpellFx.position(caster, store) == null || world == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        SpellFx.fx(FX_FIRE, center, 1.5f, store, cb);
        return new Shower(world, center, SpellFx.forward(caster, store), System.currentTimeMillis());
    }

    private static final class Comet {
        final Vector3d from, impact;
        final long startAt;
        Ref<EntityStore> model;
        boolean scaled, landed;
        Comet(Vector3d from, Vector3d impact, long startAt) { this.from = from; this.impact = impact; this.startAt = startAt; }
    }

    private static final class Shower implements Instance {
        final World world;
        final Vector3d center, slant;
        final long startAt;
        final List<Comet> comets = new ArrayList<>();

        Shower(World world, Vector3d center, Vector3d fwd, long startAt) {
            this.world = world; this.center = center; this.startAt = startAt;
            this.slant = new Vector3d(fwd.x, 0, fwd.z).normalize().mul(-SLANT);   // elles arrivent de derriere le lanceur
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            long t = now - startAt;
            if (comets.size() < COMETS && t >= FIRST_MS + comets.size() * EVERY_MS) {
                Vector3d impact = pick(caster, store);
                comets.add(new Comet(new Vector3d(impact).add(slant).add(0, DROP, 0), impact, now));
            }
            boolean flying = false;
            for (Comet c : comets) {
                if (c.landed) continue;
                flying = true;
                double k = Math.min(1.0, (now - c.startAt) / (double) FALL_MS);
                Vector3d pos = new Vector3d(c.from).lerp(c.impact, k);
                Vector3d dir = new Vector3d(c.impact).sub(c.from).normalize();
                if (c.model == null) {
                    c.model = SpellFx.spawnModel(caster, COMET, pos, dir, cb);
                    SpellFx.notSaved(c.model, store, cb);
                    SpellFx.sound(SFX_FALL, c.impact, cb);
                } else if (c.model.isValid()) {
                    if (!c.scaled) { c.scaled = true; SpellFx.setModelScale(c.model, COMET_MODEL, SCALE, 0.1, cb); }
                    SpellFx.hold(c.model, pos, SpellFx.facing(dir, true), store);
                }
                if (k >= 1.0) land(caster, c, store, cb);
            }
            return (comets.size() >= COMETS && !flying) || t >= DURATION_MS + FALL_MS;
        }

        /** Un ennemi de la zone au hasard, sinon un point au hasard au sol. */
        private Vector3d pick(Ref<EntityStore> caster, Store<EntityStore> store) {
            List<Ref<EntityStore>> enemies = SpellFx.enemiesAround(caster, center, RADIUS + 3, store);
            Collections.shuffle(enemies);
            for (Ref<EntityStore> e : enemies) {
                Vector3d p = SpellFx.position(e, store);
                if (p != null && !RuneSpellRuntime.isSummon(e) && SpellFx.horizontalDistance(p, center) <= RADIUS) return SpellFx.ground(world, p);
            }
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            double a = rng.nextDouble(Math.PI * 2), r = RADIUS * Math.sqrt(rng.nextDouble());
            return SpellFx.ground(world, new Vector3d(center).add(Math.cos(a) * r, 0, Math.sin(a) * r));
        }

        private void land(Ref<EntityStore> caster, Comet c, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            c.landed = true;
            SpellFx.remove(c.model, cb);
            Vector3d at = new Vector3d(c.impact).add(0, 0.5, 0);
            SpellFx.fx(FX_BLAST, at, 1.0f, store, cb);
            SpellFx.fx(FX_FIRE, at, 1.4f, store, cb);
            SpellFx.sound(SFX_IMPACT, c.impact, cb);
            SpellFx.shake(c.impact, 10, "Impact_Light", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, BLAST + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, c.impact) > BLAST || Math.abs(p.y - c.impact.y) > 3) continue;
                SpellFx.damage(caster, e, "Fire", DAMAGE, null, null, cb);
                SpellFx.effect(e, BURN, cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { for (Comet c : comets) if (!c.landed) SpellFx.remove(c.model, cb); }

        @Override
        public void abort() { for (Comet c : comets) if (!c.landed) RuneSpellRuntime.removeLater(c.model); }
    }
}
