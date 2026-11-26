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
 * Rune du Savoir Accelere (Temps, Epic, rune de predilection de l'Erudit Arcane) : 10 s d'esprit accelere, mana +5 % par
 * seconde et +20 % de degats elementaires (Elda_SavoirAccelere, OutgoingDamage). (Les recharges prevues par la feuille de
 * route sont predites par le client : voir Surregime.)
 * Fiche de conception : scripts/runes/specs/Rune_SavoirAccelere.json
 */
public final class SavoirAccelere implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, MANA_EVERY_MS = 1_000, ORBIT_EVERY_MS = 500;
    private static final float MANA = 0.05f;

    // ===== Assets =====
    private static final String SWIFT_MIND = "Elda_SavoirAccelere";
    private static final String FX_ORBIT = "Chronos_Gold", FX_BURST = "Chronos_Burst";
    private static final String SFX_CAST = "SFX_Arcane_Workbench_Craft";

    @Override public String id() { return "SavoirAccelere"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.effect(caster, SWIFT_MIND, cb);
        SpellFx.fx(FX_BURST, new Vector3d(pos).add(0, 1, 0), 1.2f, store, cb);
        SpellFx.sound(SFX_CAST, pos, cb);
        return new Mind(caster, System.currentTimeMillis());
    }

    private static final class Mind implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        long lastMana, lastOrbit;

        Mind(Ref<EntityStore> caster, long startAt) { this.caster = caster; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null || now - startAt >= DURATION_MS) return true;
            if (now - lastMana >= MANA_EVERY_MS) { lastMana = now; SpellFx.restore(caster, DefaultEntityStatTypes.getMana(), MANA, cb); }
            if (now - lastOrbit >= ORBIT_EVERY_MS) {                               // engrenages en orbite autour de la tete
                lastOrbit = now;
                double a = (now - startAt) / 300.0;
                SpellFx.fx(FX_ORBIT, new Vector3d(pos).add(Math.cos(a) * 0.8, 2.1, Math.sin(a) * 0.8), 0.5f, store, cb);
            }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (!caster.isValid()) return;
            try { EffectsManager.removeEffect(caster, SWIFT_MIND, cb); } catch (Exception e) { EldaniorLogger.error("SavoirAccelere: effet", e); }
        }
    }
}
