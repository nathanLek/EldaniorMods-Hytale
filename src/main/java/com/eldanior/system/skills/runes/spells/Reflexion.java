package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune de la Reflexion (Lumiere, Epic, rune de predilection de l'Archimage Supreme) : dome de 5 blocs qui suit le lanceur
 * pendant 8 s. Un projectile qui y vole vers le lanceur est detruit et son energie renvoyee en trait de lumiere sur
 * l'ennemi le plus proche de sa trajectoire inverse (le tireur, 30 blocs) : 80 degats. Les coups portes au lanceur par un
 * attaquant lui renvoient 30 % (markEcho : jamais renvoyes a nouveau).
 * Fiche de conception : scripts/runes/specs/Rune_Reflexion.json
 */
public final class Reflexion implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 8_000, RING_EVERY_MS = 400;
    private static final double RADIUS = 5.0, MIN_PROJECTILE_SPEED = 3.0, REFLECT_RANGE = 30.0, AIM_WIDTH = 3.0;
    private static final float REFLECT_DAMAGE = 80f, THORNS = 0.3f;
    private static final int RING_POINTS = 12;

    // ===== Assets =====
    private static final String FX_MIRROR = "Bifrost_Divine";
    private static final String SFX_REFLECT = "SFX_Glass_Break";

    @Override public String id() { return "Reflexion"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.fx(FX_MIRROR, new Vector3d(pos).add(0, 1, 0), 2.0f, store, cb);
        return new Mirror(caster, System.currentTimeMillis());
    }

    private record Thorn(Ref<EntityStore> attacker, float amount) {}

    private static final class Mirror implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        final Queue<Thorn> thorns = new ConcurrentLinkedQueue<>();
        final Set<Ref<EntityStore>> handled = new HashSet<>();
        long lastRing;

        Mirror(Ref<EntityStore> caster, long startAt) { this.caster = caster; this.startAt = startAt; }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            if (attacker != null && !attacker.equals(caster) && amount > 0) thorns.add(new Thorn(attacker, amount * THORNS));
            return amount;
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null || now - startAt >= DURATION_MS) return true;
            Vector3d center = new Vector3d(pos).add(0, 1, 0);
            Thorn t;
            while ((t = thorns.poll()) != null) {                              // renvoi d'une part des coups recus
                Vector3d ap = SpellFx.position(t.attacker(), store);
                if (ap == null || t.amount() < 0.5f || ap.distance(pos) > REFLECT_RANGE) continue;
                RuneSpellRuntime.markEcho(t.attacker(), t.amount());
                SpellFx.damage(caster, t.attacker(), "Elemental", t.amount(), null, null, cb);
                SpellFx.fx(FX_MIRROR, new Vector3d(ap).add(0, 1, 0), 0.6f, store, cb);
            }
            reflect(center, store, cb);
            if (now - lastRing >= RING_EVERY_MS) {
                lastRing = now;
                double turn = (now - startAt) / 1500.0;
                for (double h : new double[]{0.0, 1.6}) {
                    double r = h == 0.0 ? RADIUS : RADIUS * 0.75;
                    for (int i = 0; i < RING_POINTS; i++) {
                        double a = (h == 0.0 ? turn : -turn) + i * 2 * Math.PI / RING_POINTS;
                        SpellFx.fx(FX_MIRROR, new Vector3d(pos).add(Math.cos(a) * r, 0.3 + h * 1.8, Math.sin(a) * r), 0.3f, store, cb);
                    }
                }
            }
            return false;
        }

        /** Projectiles en vol vers le lanceur dans le dome : detruits, renvoyes en trait sur le tireur. */
        private void reflect(Vector3d center, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(center, RADIUS + 1, store)) {
                if (handled.contains(ref) || store.getComponent(ref, Projectile.getComponentType()) == null) continue;
                Velocity v = store.getComponent(ref, Velocity.getComponentType());
                Vector3d p = SpellFx.position(ref, store);
                if (v == null || p == null || v.getSpeed() < MIN_PROJECTILE_SPEED || p.distance(center) > RADIUS) continue;
                Vector3d vel = new Vector3d(v.getX(), v.getY(), v.getZ());
                if (vel.dot(new Vector3d(center).sub(p)) <= 0) continue;        // tirs du lanceur : ignores
                handled.add(ref);
                SpellFx.remove(ref, cb);
                SpellFx.sound(SFX_REFLECT, p, cb);
                Ref<EntityStore> shooter = shooter(p, new Vector3d(vel).negate().normalize(), store);
                Vector3d sp = shooter != null ? SpellFx.position(shooter, store) : null;
                if (sp == null) { SpellFx.fx(FX_MIRROR, p, 0.8f, store, cb); continue; }
                Vector3d to = new Vector3d(sp).add(0, 1, 0);
                double len = p.distance(to);
                for (double d = 0; d <= len; d += 1.5) SpellFx.fx(FX_MIRROR, new Vector3d(p).lerp(to, d / len), 0.35f, store, cb);
                SpellFx.damage(caster, shooter, "Elemental", REFLECT_DAMAGE, null, "Impact_Light", cb);
            }
        }

        /** Ennemi le plus proche de la trajectoire inverse du projectile (le tireur, en general). */
        private Ref<EntityStore> shooter(Vector3d from, Vector3d back, Store<EntityStore> store) {
            Ref<EntityStore> best = null;
            double bestScore = Double.MAX_VALUE;
            Vector3d mid = new Vector3d(from).add(new Vector3d(back).mul(REFLECT_RANGE / 2));
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, mid, REFLECT_RANGE / 2 + AIM_WIDTH, store)) {
                Vector3d q = SpellFx.position(e, store);
                if (q == null) continue;
                Vector3d to = new Vector3d(q).add(0, 1, 0).sub(from);
                double along = to.dot(back);
                if (along < 0 || along > REFLECT_RANGE) continue;
                double off = new Vector3d(to).sub(new Vector3d(back).mul(along)).length();
                if (off > AIM_WIDTH) continue;
                if (off + along * 0.05 < bestScore) { bestScore = off + along * 0.05; best = e; }
            }
            return best;
        }
    }
}
