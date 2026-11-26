package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rune de l'Arret du Temps (Temps, Unique, rune de predilection du Maitre du Temps) : les ennemis a 12 blocs (12 au plus)
 * sont figes 3 s (lockInPlace + Chronos_Fige). Les coups qu'ils recoivent pendant ce temps, de qui que ce soit, sont
 * annules et mis en reserve ({@link #absorb}, appele par DivineDamageSystem) ; a la reprise, chaque fige subit sa reserve
 * x1,25 en un seul coup.
 * Fiche de conception : scripts/runes/specs/Rune_ArretDuTemps.json
 */
public final class ArretDuTemps implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long FREEZE_MS = 3_000, GEAR_EVERY_MS = 500;
    private static final double RADIUS = 12.0;
    private static final int MAX_TARGETS = 12;
    private static final float CATCH_UP = 1.25f;

    // ===== Assets =====
    private static final String FROZEN = "Chronos_Fige";
    private static final String FX_FREEZE = "Chronos_Burst", FX_GEAR = "Chronos_Gold", FX_SHATTER = "Chronos_Shatter", FX_HIT = "Impact_Critical";
    private static final String SFX_STOP = "SFX_Crystal_Break";

    /** Cible figee -> reserve de degats de l'arret qui la retient (lu par DivineDamageSystem). */
    private static final Map<Ref<EntityStore>, Stop> frozen = new ConcurrentHashMap<>();

    /** Un coup sur une cible figee est annule et mis en reserve. true = annuler le coup. */
    public static boolean absorb(Ref<EntityStore> target, float amount) {
        if (frozen.isEmpty() || target == null) return false;
        Stop s = frozen.get(target);
        if (s == null || s.released) return false;
        s.stored.merge(target, amount, Float::sum);
        return true;
    }

    @Override public String id() { return "ArretDuTemps"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Stop s = new Stop(pos, System.currentTimeMillis());
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, RADIUS, store)) {
            if (s.stored.size() >= MAX_TARGETS) break;
            if (frozen.containsKey(e) || RuneSpellRuntime.isSummon(e)) continue;
            frozen.put(e, s);
            s.stored.put(e, 0f);
            SpellFx.lockInPlace(e, FREEZE_MS, store);
            EffectsManager.applyCustomEffect(e, FROZEN, FREEZE_MS / 1000f, com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior.OVERWRITE, cb);
            Vector3d p = SpellFx.position(e, store);
            if (p != null) SpellFx.fx(FX_FREEZE, new Vector3d(p).add(0, 1, 0), 1.4f, store, cb);
        }
        SpellFx.fx(FX_FREEZE, new Vector3d(pos).add(0, 1, 0), 3.0f, store, cb);
        SpellFx.fx(FX_GEAR, new Vector3d(pos).add(0, 3, 0), 4.0f, store, cb);
        SpellFx.sound(SFX_STOP, pos, cb);
        return s;
    }

    private static final class Stop implements Instance {
        final Vector3d center;
        final long startAt;
        final Map<Ref<EntityStore>, Float> stored = new ConcurrentHashMap<>();
        volatile boolean released;
        long lastGear;

        Stop(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (stored.isEmpty()) return true;
            if (now - startAt >= FREEZE_MS) { release(caster, store, cb); return true; }
            if (now - lastGear >= GEAR_EVERY_MS) {
                lastGear = now;
                for (Ref<EntityStore> e : stored.keySet()) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p != null) SpellFx.fx(FX_GEAR, new Vector3d(p).add(0, 2.4, 0), 0.6f, store, cb);
                }
            }
            return false;
        }

        /** Reprise du temps : chaque fige est libere et subit sa reserve x1,25 d'un coup. */
        private void release(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            released = true;
            SpellFx.fx(FX_SHATTER, new Vector3d(center).add(0, 1, 0), 3.0f, store, cb);
            SpellFx.sound(SFX_STOP, center, cb);
            for (Map.Entry<Ref<EntityStore>, Float> en : stored.entrySet()) {
                Ref<EntityStore> e = en.getKey();
                frozen.remove(e, this);
                SpellFx.unlock(e);
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                try { EffectsManager.removeEffect(e, FROZEN, cb); } catch (Exception ex) { EldaniorLogger.error("ArretDuTemps: effet", ex); }
                Vector3d at = new Vector3d(p).add(0, 1, 0);
                SpellFx.fx(FX_SHATTER, at, 1.2f, store, cb);
                float amount = en.getValue() * CATCH_UP;
                if (amount < 0.5f || SpellFx.position(caster, store) == null) continue;
                RuneSpellRuntime.markEcho(e, amount);
                SpellFx.damage(caster, e, "Elemental", amount, new Vector3d(0, 2, 0), "Impact_Strong", cb);
                SpellFx.fx(FX_HIT, at, 1.2f, store, cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            released = true;
            for (Ref<EntityStore> e : stored.keySet()) { frozen.remove(e, this); SpellFx.unlock(e); }
        }

        @Override
        public void abort() {
            released = true;
            for (Ref<EntityStore> e : stored.keySet()) { frozen.remove(e, this); SpellFx.unlock(e); }
        }
    }
}
