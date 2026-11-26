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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune du Pacte de Sang (Tenebres, Epic) : le lanceur sacrifie 20 % de ses PV max (jamais sous 1 PV) ; pendant 10 s,
 * chacun de ses coups inflige 50 % en plus (coup "echo") et lui rend 15 % du total inflige.
 * Fiche de conception : scripts/runes/specs/Rune_PacteDeSang.json
 */
public final class PacteDeSang implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000;
    private static final float SACRIFICE = 0.20f, BONUS = 0.5f, LIFESTEAL = 0.15f;

    // ===== Assets =====
    private static final String PACT = "Elda_PacteDeSang";
    private static final String FX_BLOOD = "Critical_Hit_Blood";
    private static final String SFX_PACT = "SFX_Bear_Voidtaken_Growl";

    @Override public String id() { return "PacteDeSang"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        try {
            EntityStatMap map = store.getComponent(caster, EntityStatMap.getComponentType());
            EntityStatValue hp = map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
            if (hp != null) map.subtractStatValue(DefaultEntityStatTypes.getHealth(), Math.min(hp.getMax() * SACRIFICE, Math.max(0, hp.get() - 1)));
        } catch (Exception e) {
            EldaniorLogger.error("PacteDeSang: sacrifice", e);
        }
        SpellFx.effect(caster, PACT, cb);
        SpellFx.fx(FX_BLOOD, new Vector3d(pos).add(0, 1.2, 0), 1.2f, store, cb);
        SpellFx.sound(SFX_PACT, pos, cb);
        return new Pact(caster, System.currentTimeMillis());
    }

    private static final class Pact implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        final Queue<Object[]> hits = new ConcurrentLinkedQueue<>();      // {cible, montant} des coups du lanceur

        Pact(Ref<EntityStore> caster, long startAt) { this.caster = caster; this.startAt = startAt; }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            if (hits.size() < 16) hits.add(new Object[]{target, amount});
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            float healed = 0;
            for (Object[] h = hits.poll(); h != null; h = hits.poll()) {
                @SuppressWarnings("unchecked") Ref<EntityStore> target = (Ref<EntityStore>) h[0];
                float amount = (Float) h[1], bonus = amount * BONUS;
                if (!target.isValid() || target.equals(caster) || bonus < 0.5f) continue;
                RuneSpellRuntime.markEcho(target, bonus);
                SpellFx.damage(caster, target, "Elemental", bonus, null, null, cb);
                healed += (amount + bonus) * LIFESTEAL;
            }
            SpellFx.heal(caster, healed, cb);
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (!caster.isValid()) return;
            try { EffectsManager.removeEffect(caster, PACT, cb); } catch (Exception e) { EldaniorLogger.error("PacteDeSang: effet", e); }
        }
    }
}
