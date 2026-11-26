package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de l'Eternite (Temps, Unique, rune de predilection du Magus Eternel) : pendant 10 s, aucun coup ne peut faire passer
 * le lanceur sous 1 PV (preventOwnerDeath : coup annule, PV fixes a 1). S'il a ete sauve au moins une fois, il regagne
 * 20 % de ses PV max a la fin.
 * Fiche de conception : scripts/runes/specs/Rune_Eternite.json
 */
public final class Eternite implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, AURA_EVERY_MS = 1_000;
    private static final float END_HEAL = 0.20f;

    // ===== Assets =====
    private static final String ETERNAL = "Elda_Eternite";
    private static final String FX_AURA = "Chronos_Gold", FX_SAVE = "Chronos_Burst", FX_END = "Apollo_Heal_Pulse";
    private static final String SFX_CAST = "SFX_Arcane_Workbench_Craft", SFX_SAVE = "SFX_Crystal_Break";

    @Override public String id() { return "Eternite"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.effect(caster, ETERNAL, cb);
        SpellFx.fx(FX_SAVE, new Vector3d(pos).add(0, 1, 0), 1.2f, store, cb);
        SpellFx.sound(SFX_CAST, pos, cb);
        return new Eternal(caster, System.currentTimeMillis());
    }

    private static final class Eternal implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        volatile int saves;
        long lastAura;

        Eternal(Ref<EntityStore> caster, long startAt) { this.caster = caster; this.startAt = startAt; }

        @Override
        public boolean preventOwnerDeath(Ref<EntityStore> casterRef, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (System.currentTimeMillis() - startAt >= DURATION_MS) return false;
            try {
                EntityStatMap map = store.getComponent(casterRef, EntityStatMap.getComponentType());
                if (map != null) map.setStatValue(DefaultEntityStatTypes.getHealth(), 1f);
            } catch (Exception e) {
                EldaniorLogger.error("Eternite: PV", e);
            }
            saves++;
            Vector3d p = SpellFx.position(casterRef, store);
            if (p != null) {
                SpellFx.fx(FX_SAVE, new Vector3d(p).add(0, 1, 0), 1.0f, store, cb);
                SpellFx.sound(SFX_SAVE, p, cb);
            }
            return true;
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            if (now - startAt >= DURATION_MS) {
                if (saves > 0) {                                                // l'eternite rend un peu de ce qu'elle a refuse
                    SpellFx.restore(caster, DefaultEntityStatTypes.getHealth(), END_HEAL, cb);
                    SpellFx.fx(FX_END, pos, 1.2f, store, cb);
                }
                return true;
            }
            if (now - lastAura >= AURA_EVERY_MS) { lastAura = now; SpellFx.fx(FX_AURA, new Vector3d(pos).add(0, 2.2, 0), 0.6f, store, cb); }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (!caster.isValid()) return;
            try { EffectsManager.removeEffect(caster, ETERNAL, cb); } catch (Exception e) { EldaniorLogger.error("Eternite: effet", e); }
        }
    }
}
