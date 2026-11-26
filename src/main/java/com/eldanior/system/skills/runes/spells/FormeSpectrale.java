package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Set;

/**
 * Rune de la Forme Spectrale (Tenebres, Unique) : 10 s de forme spectrale. Les coups physiques sont annules, les autres
 * reduits de moitie (RuneSpell.Instance.onOwnerHurt) ; vitesse +30 % et pas de degats de chute ; les ennemis au contact
 * (2 blocs) sont glaces.
 * Fiche de conception : scripts/runes/specs/Rune_FormeSpectrale.json
 */
public final class FormeSpectrale implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, TOUCH_EVERY_MS = 500, FALL_GRACE_MS = 2_000;
    private static final double TOUCH_RADIUS = 2.0;
    private static final float OTHER_DAMAGE = 0.5f, TOUCH_DAMAGE = 15f;
    private static final Set<String> PHYSICAL = Set.of("Physical", "Slashing", "Bludgeoning", "Crush", "Projectile");

    // ===== Assets =====
    private static final String SPECTRE = "Elda_FormeSpectrale", SLOW = "Elda_Givre";
    private static final String FX_SHIFT = "Nyx_Void", FX_TOUCH = "Effect_Snow_Impact";
    private static final String SFX_SHIFT = "SFX_Crawler_Void_Alerted";

    @Override public String id() { return "FormeSpectrale"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.effect(caster, SPECTRE, cb);
        SpellFx.protectFromFall(caster, DURATION_MS + FALL_GRACE_MS, store);
        SpellFx.fx(FX_SHIFT, pos, 1.2f, store, cb);
        SpellFx.sound(SFX_SHIFT, pos, cb);
        return new Spectre(caster, System.currentTimeMillis());
    }

    private static final class Spectre implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        long lastTouch;

        Spectre(Ref<EntityStore> caster, long startAt) { this.caster = caster; this.startAt = startAt; }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            if (causeId != null && PHYSICAL.contains(causeId)) return 0;                // les armes traversent le spectre
            return amount * OTHER_DAMAGE;
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastTouch < TOUCH_EVERY_MS) return false;
            lastTouch = now;
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, TOUCH_RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                SpellFx.damage(caster, e, "Ice", TOUCH_DAMAGE, null, null, cb);
                SpellFx.effect(e, SLOW, cb);
                SpellFx.fx(FX_TOUCH, p.add(0, 1, 0), 0.6f, store, cb);
            }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (!caster.isValid()) return;
            try { EffectsManager.removeEffect(caster, SPECTRE, cb); } catch (Exception e) { EldaniorLogger.error("FormeSpectrale: effet", e); }
        }
    }
}
