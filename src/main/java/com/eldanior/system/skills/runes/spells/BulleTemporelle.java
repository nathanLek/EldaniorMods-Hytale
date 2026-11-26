package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de la Bulle Temporelle (Temps, Epic, rune de predilection du Chronoturge) : dome de 7 blocs au point de lancement,
 * 10 s. Les allies dedans (lanceur compris) sont acceleres (Elda_BulleHate, x1,4) et regagnent 1 % de mana par seconde ;
 * les ennemis y sont ralentis (Elda_BulleLenteur, x0,5). Effets sans teinte reappliques toutes les 0,5 s.
 * Fiche de conception : scripts/runes/specs/Rune_BulleTemporelle.json
 */
public final class BulleTemporelle implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, PULSE_EVERY_MS = 500, MANA_EVERY_MS = 1_000, SOUND_EVERY_MS = 2_000;
    private static final double RADIUS = 7.0;
    private static final float MANA = 0.01f;
    private static final int RING_POINTS = 14, MOTES = 3;
    private static final double[] RING_HEIGHTS = {0.3, 0.55, 0.8};       // anneaux du dome, en fraction du rayon

    // ===== Assets =====
    private static final String HASTE = "Elda_BulleHate", SLOW = "Elda_BulleLenteur";
    private static final String FX_RING = "Chronos_Gold", FX_MOTE = "Chronos_Afterimage", FX_BURST = "Chronos_Burst";
    private static final String SFX_TICK = "SFX_Arcane_Workbench_Craft";

    @Override public String id() { return "BulleTemporelle"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.fx(FX_BURST, new Vector3d(pos).add(0, 1, 0), 2.5f, store, cb);
        return new Bubble(new Vector3d(pos), System.currentTimeMillis());
    }

    private static final class Bubble implements Instance {
        final Vector3d center;
        final long startAt;
        long lastPulse, lastMana, lastSound;

        Bubble(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            if (now - startAt >= DURATION_MS) { SpellFx.fx(FX_BURST, new Vector3d(center).add(0, 1, 0), 2.0f, store, cb); return true; }
            boolean mana = now - lastMana >= MANA_EVERY_MS;
            if (mana) lastMana = now;
            if (now - lastPulse >= PULSE_EVERY_MS) {
                lastPulse = now;
                for (Ref<EntityStore> a : SpellFx.alliesAround(caster, center, RADIUS, store)) {   // lanceur compris
                    SpellFx.effect(a, HASTE, cb);
                    if (mana) SpellFx.restore(a, DefaultEntityStatTypes.getMana(), MANA, cb);
                }
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS, store)) SpellFx.effect(e, SLOW, cb);
                dome(now, store, cb);
            }
            if (now - lastSound >= SOUND_EVERY_MS) { lastSound = now; SpellFx.sound(SFX_TICK, center, cb); }
            return false;
        }

        /** Dome : anneaux etages qui tournent lentement, poussiere de temps a l'interieur. */
        private void dome(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            double turn = (now - startAt) / 2500.0;
            SpellFx.fx(FX_RING, new Vector3d(center).add(0, RADIUS, 0), 0.8f, store, cb);
            for (double h : RING_HEIGHTS) {
                double y = RADIUS * h, r = Math.sqrt(RADIUS * RADIUS - y * y);
                int n = Math.max(6, (int) Math.round(RING_POINTS * r / RADIUS));
                for (int i = 0; i < n; i++) {
                    double a = turn * (h < 0.5 ? 1 : -1) + i * 2 * Math.PI / n;
                    SpellFx.fx(FX_RING, new Vector3d(center).add(Math.cos(a) * r, y, Math.sin(a) * r), 0.55f, store, cb);
                }
            }
            for (int i = 0; i < RING_POINTS; i++) {                            // base du dome
                double a = -turn + i * 2 * Math.PI / RING_POINTS;
                SpellFx.fx(FX_RING, new Vector3d(center).add(Math.cos(a) * RADIUS, 0.3, Math.sin(a) * RADIUS), 0.55f, store, cb);
            }
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (int i = 0; i < MOTES; i++) {
                double a = rng.nextDouble(Math.PI * 2), r = RADIUS * 0.8 * Math.sqrt(rng.nextDouble());
                SpellFx.fx(FX_MOTE, new Vector3d(center).add(Math.cos(a) * r, 0.5 + rng.nextDouble(2.0), Math.sin(a) * r), 0.9f, store, cb);
            }
        }
    }
}
