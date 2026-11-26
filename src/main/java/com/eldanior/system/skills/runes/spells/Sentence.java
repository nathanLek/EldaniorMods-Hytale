package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rune de la Sentence (Temps, Unique, rune de predilection du Prophete du Destin) : l'ennemi vise et jusqu'a 2 ennemis a
 * 6 blocs de lui sont condamnes 8 s (horloge au-dessus d'eux). Les degats que le lanceur leur inflige sont cumules
 * (onOwnerHit) ; a l'echeance, chacun les subit a nouveau (au moins 100, au plus 800 ; markEcho).
 * Fiche de conception : scripts/runes/specs/Rune_Sentence.json
 */
public final class Sentence implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 8_000, CLOCK_EVERY_MS = 500, TICK_SOUND_EVERY_MS = 2_000;
    private static final double RANGE = 25.0, SPREAD = 6.0;
    private static final int TARGETS = 3;
    private static final float MIN_DAMAGE = 100f, MAX_DAMAGE = 800f;

    // ===== Assets =====
    private static final String FX_CLOCK = "Chronos_Gold", FX_MARK = "Chronos_Burst", FX_SHATTER = "Chronos_Shatter", FX_HIT = "Impact_Critical";
    private static final String SFX_TICK = "SFX_Arcane_Workbench_Craft", SFX_DOOM = "SFX_Crystal_Break";

    @Override public String id() { return "Sentence"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Ref<EntityStore> first = SpellFx.targetEntity(caster, store, RANGE);
        if (first == null) first = nearestAhead(caster, pos, store);
        Vector3d fp = first != null ? SpellFx.position(first, store) : null;
        if (fp == null) return null;
        Map<Ref<EntityStore>, Float> doomed = new LinkedHashMap<>();
        doomed.put(first, 0f);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, fp, SPREAD, store)) {
            if (doomed.size() >= TARGETS) break;
            if (!RuneSpellRuntime.isSummon(e)) doomed.putIfAbsent(e, 0f);
        }
        for (Ref<EntityStore> e : doomed.keySet()) {
            Vector3d p = SpellFx.position(e, store);
            if (p != null) SpellFx.fx(FX_MARK, new Vector3d(p).add(0, 1, 0), 1.0f, store, cb);
        }
        SpellFx.sound(SFX_TICK, fp, cb);
        return new Doom(new ConcurrentHashMap<>(doomed), System.currentTimeMillis());
    }

    /** Ennemi le plus proche devant le lanceur (cone large), a RANGE blocs au plus. */
    private static Ref<EntityStore> nearestAhead(Ref<EntityStore> caster, Vector3d pos, Store<EntityStore> store) {
        Vector3d fwd = SpellFx.forward(caster, store);
        Ref<EntityStore> best = null;
        double bestD = Double.MAX_VALUE;
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, new Vector3d(pos).add(new Vector3d(fwd).mul(RANGE / 2)), RANGE / 2 + 1, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || RuneSpellRuntime.isSummon(e)) continue;
            Vector3d to = new Vector3d(p).sub(pos);
            double d = to.length();
            if (d > RANGE || to.setComponent(1, 0).normalize().dot(fwd) < 0.5) continue;
            if (d < bestD) { bestD = d; best = e; }
        }
        return best;
    }

    private static final class Doom implements Instance {
        final Map<Ref<EntityStore>, Float> doomed;                         // condamne -> degats du lanceur cumules
        final long startAt;
        long lastClock, lastSound;

        Doom(Map<Ref<EntityStore>, Float> doomed, long startAt) { this.doomed = doomed; this.startAt = startAt; }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            if (amount > 0) doomed.computeIfPresent(target, (k, v) -> v + amount);
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            doomed.keySet().removeIf(e -> SpellFx.position(e, store) == null);     // condamne mort ou retire
            if (doomed.isEmpty()) return true;
            if (now - startAt >= DURATION_MS) { fall(caster, store, cb); return true; }
            if (now - lastClock >= CLOCK_EVERY_MS) {
                lastClock = now;
                for (Ref<EntityStore> e : doomed.keySet()) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p != null) SpellFx.fx(FX_CLOCK, new Vector3d(p).add(0, 2.6, 0), 0.7f, store, cb);
                }
            }
            if (now - lastSound >= TICK_SOUND_EVERY_MS) {
                lastSound = now;
                for (Ref<EntityStore> e : doomed.keySet()) { Vector3d p = SpellFx.position(e, store); if (p != null) { SpellFx.sound(SFX_TICK, p, cb); break; } }
            }
            return false;
        }

        /** L'echeance : chaque condamne paie a nouveau ce qu'il a subi. */
        private void fall(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Map.Entry<Ref<EntityStore>, Float> en : doomed.entrySet()) {
                Vector3d p = SpellFx.position(en.getKey(), store);
                if (p == null) continue;
                float amount = Math.max(MIN_DAMAGE, Math.min(MAX_DAMAGE, en.getValue()));
                RuneSpellRuntime.markEcho(en.getKey(), amount);
                SpellFx.damage(caster, en.getKey(), "Elemental", amount, new Vector3d(0, 2, 0), "Impact_Strong", cb);
                Vector3d at = new Vector3d(p).add(0, 1, 0);
                SpellFx.fx(FX_SHATTER, at, 1.4f, store, cb);
                SpellFx.fx(FX_HIT, at, 1.2f, store, cb);
                SpellFx.sound(SFX_DOOM, p, cb);
            }
        }
    }
}
