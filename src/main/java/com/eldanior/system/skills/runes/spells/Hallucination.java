package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Rune de l'Hallucination (Tenebres, Epic) : brume de 8 s au point vise (rayon 10). Chaque seconde, les monstres pris
 * dedans sont apparies au hasard et forces a se battre entre eux (attitude hostile reciproque + LockedTarget) ; un monstre
 * seul est terrorise. Petits degats continus.
 * Fiche de conception : scripts/runes/specs/Rune_Hallucination.json
 */
public final class Hallucination implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 8_000, PAIR_EVERY_MS = 1_000, MIST_EVERY_MS = 3_000;
    private static final double TARGET_RANGE = 20.0, FALLBACK = 10.0, RADIUS = 10.0, HOSTILE_S = 1.5;
    private static final float DAMAGE = 8f;

    // ===== Assets =====
    private static final String DAZED = "Elda_Hallucination", TERROR = "Elda_Terreur";
    private static final String FX_MIST = "Nyx_Mist";
    private static final String SFX_CAST = "SFX_Crawler_Void_Alerted";

    @Override public String id() { return "Hallucination"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d at = SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK);
        SpellFx.sound(SFX_CAST, at, cb);
        return new Mist(caster, at, System.currentTimeMillis());
    }

    private static final class Mist implements Instance {
        final Ref<EntityStore> caster;
        final Vector3d center;
        final long startAt;
        final Set<Ref<EntityStore>> dazed = new HashSet<>();
        long lastPair, lastMist = Long.MIN_VALUE / 2;

        Mist(Ref<EntityStore> caster, Vector3d center, long startAt) { this.caster = caster; this.center = center; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastMist >= MIST_EVERY_MS) {
                lastMist = now;
                for (double dx = -RADIUS + 2.5; dx <= RADIUS - 2.5; dx += 4)
                    for (double dz = -RADIUS + 2.5; dz <= RADIUS - 2.5; dz += 4)
                        if (dx * dx + dz * dz <= RADIUS * RADIUS) SpellFx.fx(FX_MIST, new Vector3d(center).add(dx, 0.1, dz), 0.8f, store, cb);
            }
            if (now - lastPair < PAIR_EVERY_MS) return false;
            lastPair = now;
            List<Ref<EntityStore>> inside = new ArrayList<>();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + 1, store)) {
                if (RuneSpellRuntime.isSummon(e) || !CloneFx.isMonster(e, store)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, center) > RADIUS || Math.abs(p.y - center.y) > 5) continue;
                inside.add(e);
                SpellFx.damage(caster, e, "Poison", DAMAGE, null, null, cb);
                if (dazed.add(e))                                                       // visuel une seule fois (pas de clignotement)
                    EffectsManager.applyCustomEffect(e, DAZED, Math.max(0.5f, (DURATION_MS - (now - startAt)) / 1000f), OverlapBehavior.OVERWRITE, cb);
            }
            if (inside.size() == 1) { SpellFx.effect(inside.get(0), TERROR, cb); return false; }
            Collections.shuffle(inside);
            for (int i = 0; i < inside.size(); i++) {                                   // chacun s'en prend au suivant (cycle)
                Ref<EntityStore> a = inside.get(i), b = inside.get((i + 1) % inside.size());
                CloneFx.hostile(a, b, HOSTILE_S, store);
                CloneFx.hostile(b, a, HOSTILE_S, store);
                CloneFx.mark(a, "LockedTarget", b, store);
            }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> e : dazed) {
                if (!e.isValid()) continue;
                try { EffectsManager.removeEffect(e, DAZED, cb); } catch (Exception ex) { com.eldanior.system.config.EldaniorLogger.error("Hallucination: effet", ex); }
            }
        }
    }
}
