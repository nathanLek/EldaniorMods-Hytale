package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de Ruée (Combat, Common, rune propre du Guerrier) : le lanceur fonce droit devant (12 blocs, poussee entretenue
 * a chaque tick pour ne pas s'essouffler au sol) ; le premier ennemi rencontre subit 50 degats et est repousse, et la
 * charge s'arrete sur lui ou contre un mur. Protection de chute pendant la charge.
 * Fiche de conception : scripts/runes/specs/Rune_Ruee.json
 */
public final class Ruee implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 12.0, SPEED = 24.0, LIFT = 1.5, SINK = -1.5, HIT_RADIUS = 1.6;
    private static final float DAMAGE = 50f;
    private static final double PUSH = 10.0, PUSH_LIFT = 3.0;
    private static final double STALL_STEP = 0.05;                     // deplacement minimal par tick avant d'abandonner (mur)
    private static final long MAX_MS = 1_000, SUSTAIN_AFTER_MS = 120, STALL_MS = 200, DUST_EVERY_MS = 80, FALL_GUARD_MS = 2_000;

    // ===== Assets =====
    private static final String FX_DUST = "Gaia_Dust_Burst", FX_HIT = "Impact_Sword_Strong";
    private static final String SFX_HIT = "SFX_Sword_T2_Impact";

    @Override public String id() { return "Ruee"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d dir = SpellFx.forward(caster, store);
        SpellFx.protectFromFall(caster, FALL_GUARD_MS, store);
        SpellFx.push(caster, new Vector3d(dir).mul(SPEED).add(0, LIFT, 0), false, cb);
        SpellFx.fx(FX_DUST, pos, 0.6f, store, cb);
        return new Charge(pos, dir, System.currentTimeMillis());
    }

    private static final class Charge implements Instance {
        final Vector3d origin, dir, last;
        final long startAt;
        long lastDust, lastMoved;

        Charge(Vector3d origin, Vector3d dir, long startAt) {
            this.origin = origin; this.dir = dir; this.last = new Vector3d(origin);
            this.startAt = startAt; this.lastDust = startAt; this.lastMoved = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null || now - startAt >= MAX_MS || SpellFx.horizontalDistance(pos, origin) >= RANGE) return true;
            if (SpellFx.horizontalDistance(pos, last) >= STALL_STEP) lastMoved = now;
            else if (now - lastMoved >= STALL_MS) return true;           // bloque contre un mur
            last.set(pos);
            if (now - lastDust >= DUST_EVERY_MS) { SpellFx.fx(FX_DUST, pos, 0.4f, store, cb); lastDust = now; }
            Vector3d front = new Vector3d(pos).add(new Vector3d(dir).mul(0.8)).add(0, 1, 0);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, front, HIT_RADIUS + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || p.add(0, 1, 0).distance(front) > HIT_RADIUS) continue;
                SpellFx.damage(caster, e, "Physical", DAMAGE, new Vector3d(dir).mul(PUSH).add(0, PUSH_LIFT, 0), "Impact_Strong", cb);
                SpellFx.fx(FX_HIT, p, 1.0f, store, cb);
                SpellFx.sound(SFX_HIT, p, cb);
                SpellFx.push(caster, new Vector3d(dir).mul(1.0), false, cb);   // la charge s'arrete sur l'ennemi
                return true;
            }
            if (now - startAt >= SUSTAIN_AFTER_MS) {                      // apres le saut initial : vitesse entretenue, colle au sol
                SpellFx.push(caster, new Vector3d(dir).mul(SPEED).add(0, SINK, 0), false, cb);
            }
            return false;
        }
    }
}
