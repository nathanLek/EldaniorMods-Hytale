package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de la Meditation (Lumiere, Rare, rune de predilection du Sage) : 4 s de meditation, +5 % PV et +8 % mana par
 * seconde. Bouger (plus de 0,6 bloc) ou etre frappe l'interrompt (les gains deja recus restent). Menee a terme : onde
 * purifiante de 6 blocs, allies purifies (cureAllDebuffs) et soignes de 10 %, ennemis 60 degats et repousses.
 * Fiche de conception : scripts/runes/specs/Rune_Meditation.json
 */
public final class Meditation implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 4_000, GAIN_EVERY_MS = 1_000, AURA_EVERY_MS = 500;
    private static final double MAX_DRIFT = 0.6, WAVE_RADIUS = 6.0, PUSH = 9.0, PUSH_LIFT = 3.0;
    private static final float HP = 0.05f, MANA = 0.08f, WAVE_HEAL = 0.10f, WAVE_DAMAGE = 60f;
    private static final int RING_POINTS = 16;

    // ===== Assets =====
    private static final String FX_AURA = "Apollo_Heal_Pulse", FX_LIGHT = "Bifrost_Divine";
    private static final String SFX_START = "SFX_Deployable_Totem_Heal_Spawn", SFX_WAVE = "SFX_Deployable_Totem_Heal_Despawn";

    @Override public String id() { return "Meditation"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.sound(SFX_START, pos, cb);
        return new Calm(new Vector3d(pos), System.currentTimeMillis());
    }

    private static final class Calm implements Instance {
        final Vector3d origin;
        final long startAt;
        volatile boolean hurt;
        long lastGain, lastAura;

        Calm(Vector3d origin, long startAt) { this.origin = origin; this.startAt = startAt; this.lastGain = startAt; }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            if (amount > 0) hurt = true;                                         // un coup brise la meditation
            return amount;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null || hurt || SpellFx.horizontalDistance(pos, origin) > MAX_DRIFT) return true;
            if (now - lastGain >= GAIN_EVERY_MS) {
                lastGain = now;
                SpellFx.restore(caster, DefaultEntityStatTypes.getHealth(), HP, cb);
                SpellFx.restore(caster, DefaultEntityStatTypes.getMana(), MANA, cb);
                SpellFx.fx(FX_LIGHT, new Vector3d(pos).add(0, 2.3, 0), 0.6f, store, cb);
            }
            if (now - lastAura >= AURA_EVERY_MS) { lastAura = now; SpellFx.fx(FX_AURA, pos, 0.9f, store, cb); }
            if (now - startAt >= DURATION_MS) { wave(caster, pos, store, cb); return true; }
            return false;
        }

        /** Onde purifiante : allies purifies et soignes, ennemis blesses et repousses. */
        private void wave(Ref<EntityStore> caster, Vector3d pos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.fx(FX_AURA, pos, 2.5f, store, cb);
            for (int i = 0; i < RING_POINTS; i++) {
                double a = i * 2 * Math.PI / RING_POINTS;
                SpellFx.fx(FX_LIGHT, new Vector3d(pos).add(Math.cos(a) * WAVE_RADIUS, 0.6, Math.sin(a) * WAVE_RADIUS), 0.5f, store, cb);
            }
            SpellFx.sound(SFX_WAVE, pos, cb);
            for (Ref<EntityStore> a : SpellFx.alliesAround(caster, pos, WAVE_RADIUS, store)) {   // lanceur compris
                try { EffectsManager.cureAllDebuffs(a, cb); } catch (Exception e) { EldaniorLogger.error("Meditation: purification", e); }
                SpellFx.restore(a, DefaultEntityStatTypes.getHealth(), WAVE_HEAL, cb);
            }
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, WAVE_RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d away = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
                if (away.lengthSquared() < 1e-6) away.set(1, 0, 0);
                SpellFx.damage(caster, e, "Elemental", WAVE_DAMAGE, away.normalize().mul(PUSH).add(0, PUSH_LIFT, 0), "Impact_Light", cb);
            }
        }
    }
}
