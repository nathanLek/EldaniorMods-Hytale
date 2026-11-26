package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune de la Pierre Philosophale (Lumiere, Epic, rune de predilection du Philosophe Mage) : 10 s pendant lesquelles le
 * lanceur et ses allies a 8 blocs (au moment de l'incantation) regagnent 3 % de PV par seconde, sont immunises au poison
 * (Elda_PierrePhilosophale, DamageResistance Poison 100 %) et purifies de leurs malus toutes les 2 s.
 * Fiche de conception : scripts/runes/specs/Rune_PierrePhilosophale.json
 */
public final class PierrePhilosophale implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, HEAL_EVERY_MS = 1_000, CURE_EVERY_MS = 2_000, STONE_EVERY_MS = 400;
    private static final double RADIUS = 8.0, STONE_HEIGHT = 2.6;
    private static final float HEAL = 0.03f;

    // ===== Assets =====
    private static final String BLESSING = "Elda_PierrePhilosophale";
    private static final String FX_STONE = "Bifrost_Divine", FX_EMBER = "Impact_Fire", FX_HEAL = "Apollo_Heal_Pulse";
    private static final String SFX_CAST = "SFX_Deployable_Totem_Heal_Spawn";

    @Override public String id() { return "PierrePhilosophale"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        List<Ref<EntityStore>> blessed = new ArrayList<>(SpellFx.alliesAround(caster, pos, RADIUS, store));   // lanceur compris
        for (Ref<EntityStore> a : blessed) {
            SpellFx.effect(a, BLESSING, cb);
            try { EffectsManager.cureAllDebuffs(a, cb); } catch (Exception e) { EldaniorLogger.error("PierrePhilosophale: purification", e); }
        }
        SpellFx.fx(FX_STONE, new Vector3d(pos).add(0, STONE_HEIGHT, 0), 2.0f, store, cb);
        SpellFx.sound(SFX_CAST, pos, cb);
        return new Stone(caster, blessed, System.currentTimeMillis());
    }

    private static final class Stone implements Instance {
        final Ref<EntityStore> caster;
        final List<Ref<EntityStore>> blessed;
        final long startAt;
        long lastHeal, lastCure, lastStone;

        Stone(Ref<EntityStore> caster, List<Ref<EntityStore>> blessed, long startAt) {
            this.caster = caster; this.blessed = blessed; this.startAt = startAt; this.lastCure = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null || now - startAt >= DURATION_MS) return true;
            blessed.removeIf(a -> !a.isValid());
            boolean heal = now - lastHeal >= HEAL_EVERY_MS, cure = now - lastCure >= CURE_EVERY_MS;
            if (heal) lastHeal = now;
            if (cure) lastCure = now;
            for (Ref<EntityStore> a : blessed) {
                if (heal) SpellFx.restore(a, DefaultEntityStatTypes.getHealth(), HEAL, cb);
                if (cure) {
                    try { EffectsManager.cureAllDebuffs(a, cb); } catch (Exception e) { EldaniorLogger.error("PierrePhilosophale: purification", e); }
                    Vector3d p = SpellFx.position(a, store);
                    if (p != null) SpellFx.fx(FX_HEAL, p, 0.8f, store, cb);
                }
            }
            if (now - lastStone >= STONE_EVERY_MS) {                               // la pierre flotte et rougeoie au-dessus du lanceur
                lastStone = now;
                Vector3d at = new Vector3d(pos).add(0, STONE_HEIGHT + Math.sin(now / 300.0) * 0.12, 0);
                SpellFx.fx(FX_STONE, at, 0.5f, store, cb);
                SpellFx.fx(FX_EMBER, at, 0.3f, store, cb);
            }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> a : blessed) {
                if (!a.isValid()) continue;
                try { EffectsManager.removeEffect(a, BLESSING, cb); } catch (Exception e) { EldaniorLogger.error("PierrePhilosophale: effet", e); }
            }
        }
    }
}
