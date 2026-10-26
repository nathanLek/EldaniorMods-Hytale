package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune du Lien d'Ame (Lumiere, Rare) : lie le lanceur a l'allie vise pendant 8 s. L'allie subit 20 % de degats en moins
 * (effet), et toutes les 0,5 s jusqu'a 4 % de PV passent de celui qui en a le plus (en proportion) a l'autre, sans
 * jamais faire descendre le donneur sous 10 %. Le lien se rompt au-dela de 30 blocs.
 * Fiche de conception : scripts/runes/specs/Rune_LienDAme.json
 */
public final class LienDAme implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 8_000, PULSE_EVERY_MS = 500;
    private static final double RANGE = 25.0, BREAK_DISTANCE = 30.0;
    private static final float TRANSFER = 0.04f, DONOR_FLOOR = 0.10f, MIN_GAP = 0.05f;

    // ===== Assets =====
    private static final String LINK = "Elda_LienDAme";
    private static final String FX_THREAD = "Bifrost_Divine", FX_HEAL = "Apollo_Heal_Pulse";
    private static final String SFX_LINK = "SFX_Portal_Void";

    @Override public String id() { return "LienDAme"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> ally = SpellFx.targetLiving(caster, store, RANGE);
        if (ally == null || !SpellFx.isAlly(caster, ally, store)) return null;
        Vector3d p = SpellFx.position(ally, store);
        if (p == null) return null;
        SpellFx.effect(ally, LINK, cb);
        SpellFx.fx(FX_HEAL, p.add(0, 1, 0), 1.0f, store, cb);
        SpellFx.sound(SFX_LINK, p, cb);
        return new Link(caster, ally, System.currentTimeMillis());
    }

    private static final class Link implements Instance {
        final Ref<EntityStore> caster, ally;
        final long startAt;
        long lastPulse;

        Link(Ref<EntityStore> caster, Ref<EntityStore> ally, long startAt) { this.caster = caster; this.ally = ally; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            Vector3d a = SpellFx.position(caster, store), b = SpellFx.position(ally, store);
            if (a == null || b == null || a.distance(b) > BREAK_DISTANCE) return true;
            if (now - lastPulse < PULSE_EVERY_MS) return false;
            lastPulse = now;
            a.add(0, 1.2, 0);
            b.add(0, 1.2, 0);
            for (double t = 0; t <= 1.0; t += 1.0 / Math.max(2, (int) (a.distance(b) / 1.5)))
                SpellFx.fx(FX_THREAD, new Vector3d(a).lerp(b, t), 0.2f, store, cb);
            share(store, cb, b);
            return false;
        }

        /** Le plus vaillant (en proportion de ses PV max) donne au plus blesse. */
        private void share(Store<EntityStore> store, CommandBuffer<EntityStore> cb, Vector3d allyAt) {
            int hp = DefaultEntityStatTypes.getHealth();
            EntityStatMap mc = store.getComponent(caster, EntityStatMap.getComponentType());
            EntityStatMap ma = store.getComponent(ally, EntityStatMap.getComponentType());
            if (mc == null || ma == null) return;
            EntityStatValue vc = mc.get(hp), va = ma.get(hp);
            if (vc == null || va == null || vc.getMax() <= 0 || va.getMax() <= 0) return;
            float fc = vc.get() / vc.getMax(), fa = va.get() / va.getMax();
            if (Math.abs(fc - fa) < MIN_GAP) return;
            boolean casterGives = fc > fa;
            EntityStatMap donorMap = casterGives ? mc : ma, takerMap = casterGives ? ma : mc;
            EntityStatValue donor = casterGives ? vc : va, taker = casterGives ? va : vc;
            float share = Math.min(TRANSFER, Math.abs(fc - fa) / 2);                    // jamais plus que l'ecart
            float give = Math.min(share * donor.getMax(), donor.get() - DONOR_FLOOR * donor.getMax());
            if (give <= 0) return;
            try {
                donorMap.subtractStatValue(hp, give);
                takerMap.addStatValue(hp, Math.min(give, taker.getMax() - taker.get()));
            } catch (Exception e) {
                EldaniorLogger.error("LienDAme: transfert", e);
                return;
            }
            if (casterGives) SpellFx.fx(FX_HEAL, allyAt, 0.6f, store, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (!ally.isValid()) return;
            try { EffectsManager.removeEffect(ally, LINK, cb); } catch (Exception e) { EldaniorLogger.error("LienDAme: effet", e); }
        }
    }
}
