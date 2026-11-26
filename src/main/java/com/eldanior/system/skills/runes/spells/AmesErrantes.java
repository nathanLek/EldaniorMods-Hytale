package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune des Ames Errantes (Tenebres, Rare) : quatre ames (particules) tournent autour du lanceur pendant 12 s. Des qu'un
 * ennemi s'approche a moins de 6 blocs, ou frappe le lanceur (15 blocs), une ame fond sur lui (une par seconde, guidee),
 * le blesse et soigne le lanceur. Le dernier agresseur est vise en priorite.
 * Fiche de conception : scripts/runes/specs/Rune_AmesErrantes.json
 */
public final class AmesErrantes implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 12_000, LAUNCH_EVERY_MS = 1_000, GLOW_EVERY_MS = 150, FLIGHT_MAX_MS = 2_000;
    private static final int SOULS = 4;
    private static final double GUARD_RANGE = 6.0, RIPOSTE_RANGE = 15.0, ORBIT = 1.6, ORBIT_SPEED = 2.2, SPEED = 18.0, HIT = 0.9;
    private static final float DAMAGE = 50f, HEAL = 0.03f;

    // ===== Assets =====
    private static final String FX_SOUL = "Nyx_Drain_Burst", FX_HIT = "Nyx_Void";
    private static final String SFX_LAUNCH = "SFX_Crawler_Void_Alerted";

    @Override public String id() { return "AmesErrantes"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.sound(SFX_LAUNCH, pos, cb);
        return new Souls(caster, System.currentTimeMillis());
    }

    /** Ame lancee vers sa cible. */
    private static final class Flight {
        final Vector3d pos;
        final Ref<EntityStore> target;
        final long born;
        Flight(Vector3d pos, Ref<EntityStore> target, long born) { this.pos = pos; this.target = target; this.born = born; }
    }

    private static final class Souls implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        final List<Flight> flights = new ArrayList<>();
        int orbiting = SOULS;
        volatile Ref<EntityStore> attacker;                         // dernier agresseur du lanceur (riposte prioritaire)
        long last, lastLaunch, lastGlow;

        Souls(Ref<EntityStore> caster, long startAt) { this.caster = caster; this.startAt = startAt; this.last = startAt; }

        @Override
        public float onOwnerHurt(Ref<EntityStore> from, float amount, String causeId) {
            if (from != null && !from.equals(caster)) attacker = from;                // memorise seulement (riposte dans tick)
            return amount;
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            boolean expired = now - startAt >= DURATION_MS;
            if (expired) orbiting = 0;                                                // les ames en orbite se dissipent
            double dt = Math.min(0.2, (now - last) / 1000.0);
            last = now;
            if (orbiting > 0 && now - lastGlow >= GLOW_EVERY_MS) {
                lastGlow = now;
                for (int i = 0; i < orbiting; i++) SpellFx.fx(FX_SOUL, orbitPoint(pos, i, now), 0.5f, store, cb);
            }
            if (orbiting > 0 && now - lastLaunch >= LAUNCH_EVERY_MS) {
                Ref<EntityStore> threat = riposte(pos, store);
                if (threat == null) threat = nearest(pos, store);
                if (threat != null) {
                    lastLaunch = now;
                    orbiting--;
                    flights.add(new Flight(orbitPoint(pos, orbiting, now), threat, now));
                    SpellFx.sound(SFX_LAUNCH, pos, cb);
                }
            }
            flights.removeIf(f -> fly(f, dt, now, store, cb));
            return orbiting == 0 && flights.isEmpty();
        }

        private Vector3d orbitPoint(Vector3d center, int i, long now) {
            double a = (now - startAt) / 1000.0 * ORBIT_SPEED + i * Math.PI * 2 / SOULS;
            return new Vector3d(center).add(Math.cos(a) * ORBIT, 1.2 + Math.sin(a * 2) * 0.15, Math.sin(a) * ORBIT);
        }

        /** Vol guide vers le torse de la cible ; true quand l'ame a frappe ou s'est dissipee. */
        private boolean fly(Flight f, double dt, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - f.born > FLIGHT_MAX_MS || !f.target.isValid() || store.getComponent(f.target, DeathComponent.getComponentType()) != null) return true;
            Vector3d tp = SpellFx.position(f.target, store);
            if (tp == null) return true;
            tp.add(0, 1, 0);
            Vector3d to = new Vector3d(tp).sub(f.pos);
            double d = to.length();
            if (d <= HIT + SPEED * dt) {
                SpellFx.fx(FX_HIT, tp, 0.6f, store, cb);
                SpellFx.damage(caster, f.target, "Elemental", DAMAGE, null, "Impact_Light", cb);
                SpellFx.restore(caster, DefaultEntityStatTypes.getHealth(), HEAL, cb);
                return true;
            }
            f.pos.add(to.div(d).mul(SPEED * dt));
            SpellFx.fx(FX_SOUL, f.pos, 0.6f, store, cb);
            return false;
        }

        /** Le dernier agresseur, s'il est encore un ennemi vivant a portee de riposte. */
        private Ref<EntityStore> riposte(Vector3d from, Store<EntityStore> store) {
            Ref<EntityStore> a = attacker;
            attacker = null;
            if (a == null || !a.isValid() || store.getComponent(a, DeathComponent.getComponentType()) != null || !SpellFx.isEnemy(caster, a, store)) return null;
            Vector3d p = SpellFx.position(a, store);
            return p != null && p.distance(from) <= RIPOSTE_RANGE ? a : null;
        }

        private Ref<EntityStore> nearest(Vector3d from, Store<EntityStore> store) {
            Ref<EntityStore> best = null;
            double bestD = GUARD_RANGE * GUARD_RANGE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, from, GUARD_RANGE, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                double d = p.distanceSquared(from);
                if (d < bestD) { bestD = d; best = e; }
            }
            return best;
        }
    }
}
