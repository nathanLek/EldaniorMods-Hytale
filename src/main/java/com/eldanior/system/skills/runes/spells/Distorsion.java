package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de Distorsion (Temps, Epic, rune de predilection de l'Oracle) : zone de temps distordu de 10 s (rayon 8) au point
 * vise. Les ennemis dedans sont ralentis (Elda_Distorsion, vitesse x0,4, reappliquee toutes les 0,5 s) ; les projectiles
 * en vol vers le lanceur ou ses allies s'y figent (maintenus en place), puis se dissipent a la fin de la zone.
 * Fiche de conception : scripts/runes/specs/Rune_Distorsion.json
 */
public final class Distorsion implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, SLOW_EVERY_MS = 500, RING_EVERY_MS = 500, SOUND_EVERY_MS = 2_000;
    private static final double RANGE = 20.0, RADIUS = 8.0, MIN_PROJECTILE_SPEED = 3.0;
    private static final int RING_POINTS = 16, MOTES = 4;

    // ===== Assets =====
    private static final String SLOW = "Elda_Distorsion";
    private static final String FX_RING = "Chronos_Gold", FX_MOTE = "Chronos_Afterimage", FX_BURST = "Chronos_Burst", FX_FIZZLE = "Chronos_Shatter";
    private static final String SFX_TICK = "SFX_Arcane_Workbench_Craft";

    @Override public String id() { return "Distorsion"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        SpellFx.fx(FX_BURST, new Vector3d(center).add(0, 1, 0), 2.5f, store, cb);
        return new Zone(center, System.currentTimeMillis());
    }

    private static final class Zone implements Instance {
        final Vector3d center;
        final long startAt;
        final Map<Ref<EntityStore>, Vector3d> frozen = new HashMap<>();       // projectile fige -> position
        long lastSlow, lastRing, lastSound;

        Zone(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d casterPos = SpellFx.position(caster, store);
            if (casterPos == null) return true;
            if (now - startAt >= DURATION_MS) {
                SpellFx.fx(FX_BURST, new Vector3d(center).add(0, 1, 0), 2.0f, store, cb);
                for (Vector3d p : frozen.values()) SpellFx.fx(FX_FIZZLE, p, 0.5f, store, cb);   // les projectiles figes se dissipent
                return true;
            }
            freeze(casterPos, store, cb);
            if (now - lastSlow >= SLOW_EVERY_MS) {
                lastSlow = now;
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS, store)) SpellFx.effect(e, SLOW, cb);
            }
            if (now - lastRing >= RING_EVERY_MS) {                                // bord de la zone + poussiere de temps dedans
                lastRing = now;
                double turn = (now - startAt) / 3000.0;
                for (int i = 0; i < RING_POINTS; i++) {
                    double a = turn + i * 2 * Math.PI / RING_POINTS;
                    SpellFx.fx(FX_RING, new Vector3d(center).add(Math.cos(a) * RADIUS, 0.4, Math.sin(a) * RADIUS), 0.6f, store, cb);
                }
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                for (int i = 0; i < MOTES; i++) {
                    double a = rng.nextDouble(Math.PI * 2), r = RADIUS * Math.sqrt(rng.nextDouble());
                    SpellFx.fx(FX_MOTE, new Vector3d(center).add(Math.cos(a) * r, 0.5 + rng.nextDouble(1.5), Math.sin(a) * r), 1.0f, store, cb);
                }
            }
            if (now - lastSound >= SOUND_EVERY_MS) { lastSound = now; SpellFx.sound(SFX_TICK, center, cb); }
            return false;
        }

        /** Fige les projectiles qui volent vers le lanceur dans la zone ; maintient ceux deja figes. */
        private void freeze(Vector3d casterPos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> ref : TargetUtil.getAllEntitiesInSphere(center, RADIUS + 1, store)) {
                if (frozen.containsKey(ref) || store.getComponent(ref, Projectile.getComponentType()) == null) continue;
                Velocity v = store.getComponent(ref, Velocity.getComponentType());
                Vector3d p = SpellFx.position(ref, store);
                if (v == null || p == null || v.getSpeed() < MIN_PROJECTILE_SPEED || p.distance(center) > RADIUS) continue;
                Vector3d toCaster = new Vector3d(casterPos).add(0, 1, 0).sub(p);
                if (v.getX() * toCaster.x + v.getY() * toCaster.y + v.getZ() * toCaster.z <= 0) continue;   // tirs du lanceur : jamais figes
                frozen.put(ref, p);
                SpellFx.fx(FX_MOTE, p, 0.6f, store, cb);
            }
            frozen.entrySet().removeIf(en -> !en.getKey().isValid());
            for (Map.Entry<Ref<EntityStore>, Vector3d> en : frozen.entrySet()) SpellFx.hold(en.getKey(), en.getValue(), null, store);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> ref : frozen.keySet()) SpellFx.remove(ref, cb);
            frozen.clear();
        }

        @Override
        public void abort() {
            for (Ref<EntityStore> ref : frozen.keySet()) RuneSpellRuntime.removeLater(ref);
        }
    }
}
