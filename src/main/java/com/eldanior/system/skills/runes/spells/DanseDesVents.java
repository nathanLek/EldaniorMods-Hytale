package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de la Danse des Vents (Air, Unique) : 15 s portees par le vent (vitesse +50 %, pas de degats de chute) ; chaque
 * clic droit est un bond dans la direction du regard qui libere une rafale autour du point de depart (8 bonds max,
 * 1 par seconde).
 * Fiche de conception : scripts/runes/specs/Rune_DanseDesVents.json
 */
public final class DanseDesVents implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 15_000, LEAP_EVERY_MS = 1_000, FALL_GRACE_MS = 3_000;
    private static final int MAX_LEAPS = 8;
    private static final double LEAP_SPEED = 14.0, LEAP_LIFT = 9.0, GUST_RADIUS = 5.0, GUST_PUSH = 12.0, GUST_LIFT = 4.0;
    private static final float GUST_DAMAGE = 45f;

    // ===== Assets =====
    private static final String LIGHTNESS = "Elda_Legerete";
    private static final String FX_CANNON = "Fujin_Cannon", FX_GUST = "Fujin_Gust";
    private static final String SFX_GUST = "SFX_Ability_Wind_Strike_Slash";

    @Override public String id() { return "DanseDesVents"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        EffectsManager.applyCustomEffect(caster, LIGHTNESS, DURATION_MS / 1000f, OverlapBehavior.OVERWRITE, cb);
        SpellFx.protectFromFall(caster, DURATION_MS + FALL_GRACE_MS, store);
        SpellFx.fx(FX_CANNON, new Vector3d(pos).add(0, 0.5, 0), 0f, 90f, 0.5f, store, cb);          // vers le haut
        SpellFx.sound(SFX_GUST, pos, cb);
        return new Dance(caster, System.currentTimeMillis());
    }

    private static final class Dance implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        long lastLeap;
        int leaps;
        boolean leapRequested;

        Dance(Ref<EntityStore> caster, long startAt) { this.caster = caster; this.startAt = startAt; }

        @Override public boolean listensClicks() { return true; }

        @Override
        public void onClick(boolean left, Ref<EntityStore> target, Ref<EntityStore> casterRef, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!left) leapRequested = true;                                          // applique dans tick
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS || leaps >= MAX_LEAPS) return true;
            if (!leapRequested) return false;
            leapRequested = false;
            if (now - lastLeap < LEAP_EVERY_MS) return false;
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            lastLeap = now;
            leaps++;
            Vector3d look = SpellFx.lookDirection(caster, store);
            Vector3d flat = new Vector3d(look.x, 0, look.z);
            if (flat.lengthSquared() < 1e-6) flat.set(0, 0, 1);
            flat.normalize();
            SpellFx.push(caster, new Vector3d(flat).mul(LEAP_SPEED).add(0, LEAP_LIFT + Math.max(0, look.y) * 4, 0), false, cb);
            gust(pos, store, cb);
            return false;
        }

        /** Rafale en anneau autour du point de depart du bond. */
        private void gust(Vector3d pos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (int i = 0; i < 8; i++) {
                double a = Math.PI * 2 * i / 8;
                SpellFx.fx(FX_GUST, new Vector3d(pos).add(Math.cos(a) * 2.5, 0.6, Math.sin(a) * 2.5), 0.8f, store, cb);
            }
            SpellFx.sound(SFX_GUST, pos, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, GUST_RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d out = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
                if (out.lengthSquared() < 0.25) out.set(0, 0, 1);
                SpellFx.damage(caster, e, "Wind", GUST_DAMAGE, out.normalize().mul(GUST_PUSH).add(0, GUST_LIFT, 0), "Impact_Light", cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (!caster.isValid()) return;
            try { EffectsManager.removeEffect(caster, LIGHTNESS, cb); } catch (Exception e) { EldaniorLogger.error("DanseDesVents: effet", e); }
        }
    }
}
