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
 * Rune de la Fontaine de Vie (Lumiere, Rare) : source de lumiere de 6 s au point vise. A son apparition, elle purifie
 * les allies proches (et le lanceur) de leurs malus ; ensuite elle leur rend 5 % de PV chaque seconde.
 * Fiche de conception : scripts/runes/specs/Rune_FontaineDeVie.json
 */
public final class FontaineDeVie implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, HEAL_EVERY_MS = 1_000, JET_EVERY_MS = 500;
    private static final double TARGET_RANGE = 20.0, FALLBACK = 4.0, RADIUS = 5.0;
    private static final float HEAL = 0.05f;
    private static final int RING_POINTS = 12;

    // ===== Assets =====
    private static final String FX_JET = "Bifrost_Divine", FX_HEAL = "Apollo_Heal_Pulse";
    private static final String SFX_RISE = "SFX_Portal_Void";

    @Override public String id() { return "FontaineDeVie"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d at = SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK);
        Fountain f = new Fountain(at, System.currentTimeMillis());
        for (Ref<EntityStore> a : f.allies(caster, store)) {
            try { EffectsManager.cureAllDebuffs(a, cb); } catch (Exception e) { EldaniorLogger.error("FontaineDeVie: purification", e); }
        }
        SpellFx.fx(FX_JET, new Vector3d(at).add(0, 1, 0), 1.2f, store, cb);
        SpellFx.sound(SFX_RISE, at, cb);
        return f;
    }

    private static final class Fountain implements Instance {
        final Vector3d at;
        final long startAt;
        long lastHeal, lastJet;

        Fountain(Vector3d at, long startAt) { this.at = at; this.startAt = startAt; this.lastHeal = startAt; }

        /** Allies dans le rayon, lanceur compris. */
        List<Ref<EntityStore>> allies(Ref<EntityStore> caster, Store<EntityStore> store) {
            List<Ref<EntityStore>> out = new ArrayList<>(SpellFx.alliesAround(caster, at, RADIUS, store));
            Vector3d cp = SpellFx.position(caster, store);
            if (cp != null && cp.distance(at) <= RADIUS + 1 && !out.contains(caster)) out.add(caster);
            return out;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastJet >= JET_EVERY_MS) {
                lastJet = now;
                for (double h = 0.5; h <= 3.0; h += 0.8) SpellFx.fx(FX_JET, new Vector3d(at).add(0, h, 0), 0.35f, store, cb);
                for (int i = 0; i < RING_POINTS; i++) {
                    double a = Math.PI * 2 * i / RING_POINTS;
                    SpellFx.fx(FX_JET, new Vector3d(at).add(Math.cos(a) * RADIUS, 0.3, Math.sin(a) * RADIUS), 0.25f, store, cb);
                }
            }
            if (now - lastHeal >= HEAL_EVERY_MS) {
                lastHeal = now;
                for (Ref<EntityStore> a : allies(caster, store)) {
                    SpellFx.restore(a, DefaultEntityStatTypes.getHealth(), HEAL, cb);
                    Vector3d p = SpellFx.position(a, store);
                    if (p != null) SpellFx.fx(FX_HEAL, p.add(0, 1, 0), 0.8f, store, cb);
                }
            }
            return false;
        }
    }
}
