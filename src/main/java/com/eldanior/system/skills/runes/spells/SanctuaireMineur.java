package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

/**
 * Rune du Sanctuaire Mineur (Lumiere, Epic, rune de predilection du Grand Sage) : dome de lumiere fixe de 6 blocs, 8 s.
 * Les allies dedans (lanceur compris) ont Elda_Sanctuaire (-40 % de degats, pose a l'entree, retire a la sortie), sont
 * purifies et liberes en permanence (cureAllDebuffs + unlock toutes les 0,5 s) et regagnent 1 % de PV par seconde.
 * Fiche de conception : scripts/runes/specs/Rune_SanctuaireMineur.json
 */
public final class SanctuaireMineur implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 8_000, PULSE_EVERY_MS = 500, HEAL_EVERY_MS = 1_000;
    private static final double RADIUS = 6.0;
    private static final float HEAL = 0.01f;
    private static final int RING_POINTS = 14;

    // ===== Assets =====
    private static final String SHELTER = "Elda_Sanctuaire";
    private static final String FX_DOME = "Bifrost_Divine", FX_HEAL = "Apollo_Heal_Pulse";
    private static final String SFX_OPEN = "SFX_Deployable_Totem_Heal_Spawn", SFX_CLOSE = "SFX_Deployable_Totem_Heal_Despawn";

    @Override public String id() { return "SanctuaireMineur"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.fx(FX_HEAL, pos, 2.0f, store, cb);
        SpellFx.sound(SFX_OPEN, pos, cb);
        return new Shelter(new Vector3d(pos), System.currentTimeMillis());
    }

    private static final class Shelter implements Instance {
        final Vector3d center;
        final long startAt;
        final Set<Ref<EntityStore>> sheltered = new HashSet<>();
        long lastPulse, lastHeal;

        Shelter(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            if (now - startAt >= DURATION_MS) { SpellFx.sound(SFX_CLOSE, center, cb); return true; }
            if (now - lastPulse < PULSE_EVERY_MS) return false;
            lastPulse = now;
            boolean heal = now - lastHeal >= HEAL_EVERY_MS;
            if (heal) lastHeal = now;
            Set<Ref<EntityStore>> inside = new HashSet<>(SpellFx.alliesAround(caster, center, RADIUS, store));   // lanceur compris
            float left = Math.max(0.1f, (DURATION_MS - (now - startAt)) / 1000f);
            for (Ref<EntityStore> a : inside) {
                if (sheltered.add(a)) EffectsManager.applyCustomEffect(a, SHELTER, left, OverlapBehavior.OVERWRITE, cb);
                try { EffectsManager.cureAllDebuffs(a, cb); } catch (Exception e) { EldaniorLogger.error("SanctuaireMineur: purification", e); }
                SpellFx.unlock(a);
                if (heal) SpellFx.restore(a, DefaultEntityStatTypes.getHealth(), HEAL, cb);
            }
            sheltered.removeIf(a -> {
                if (inside.contains(a)) return false;
                unshelter(a, cb);
                return true;
            });
            dome(now, store, cb);
            if (heal) SpellFx.fx(FX_HEAL, center, 1.2f, store, cb);
            return false;
        }

        private void dome(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            double turn = (now - startAt) / 2000.0;
            SpellFx.fx(FX_DOME, new Vector3d(center).add(0, RADIUS, 0), 0.6f, store, cb);
            for (double h : new double[]{0.05, 0.6}) {
                double y = RADIUS * h, r = Math.sqrt(RADIUS * RADIUS - y * y);
                for (int i = 0; i < RING_POINTS; i++) {
                    double a = (h < 0.5 ? turn : -turn) + i * 2 * Math.PI / RING_POINTS;
                    SpellFx.fx(FX_DOME, new Vector3d(center).add(Math.cos(a) * r, y + 0.2, Math.sin(a) * r), 0.35f, store, cb);
                }
            }
        }

        private static void unshelter(Ref<EntityStore> a, CommandBuffer<EntityStore> cb) {
            if (!a.isValid()) return;
            try { EffectsManager.removeEffect(a, SHELTER, cb); } catch (Exception e) { EldaniorLogger.error("SanctuaireMineur: effet", e); }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> a : sheltered) unshelter(a, cb);
            sheltered.clear();
        }
    }
}
