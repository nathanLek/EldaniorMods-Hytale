package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune de la Marque Funeste (Tenebres, Common) : maudit l'ennemi vise 8 s ; chaque coup du lanceur sur lui (sorts
 * compris) inflige 15 % en plus. Le bonus est un coup "echo" (il ne se redeclenche pas lui-meme).
 * Fiche de conception : scripts/runes/specs/Rune_MarqueFuneste.json
 */
public final class MarqueFuneste implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 8_000;
    private static final double RANGE = 20.0;
    private static final float DAMAGE = 25f, BONUS = 0.15f;

    // ===== Assets =====
    private static final String CURSE = "Elda_MarqueFuneste";
    private static final String FX_MARK = "Nyx_Void";
    private static final String SFX_MARK = "SFX_Crawler_Void_Alerted";

    @Override public String id() { return "MarqueFuneste"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> target = SpellFx.targetEntity(caster, store, RANGE);
        Vector3d p = SpellFx.position(target, store);
        if (p == null) return null;
        SpellFx.damage(caster, target, "Elemental", DAMAGE, null, null, cb);
        SpellFx.effect(target, CURSE, cb);
        SpellFx.fx(FX_MARK, p, 0.8f, store, cb);
        SpellFx.sound(SFX_MARK, p, cb);
        return new Curse(target, System.currentTimeMillis());
    }

    private static final class Curse implements Instance {
        final Ref<EntityStore> target;
        final long startAt;
        final Queue<Float> hits = new ConcurrentLinkedQueue<>();   // coups du lanceur sur la cible (traitement des degats)

        Curse(Ref<EntityStore> target, long startAt) { this.target = target; this.startAt = startAt; }

        @Override
        public void onOwnerHit(Ref<EntityStore> t, float amount) {
            if (t.equals(target) && hits.size() < 16) hits.add(amount);
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS || !target.isValid() || store.getComponent(target, DeathComponent.getComponentType()) != null) return true;
            for (Float amount = hits.poll(); amount != null; amount = hits.poll()) {
                float bonus = amount * BONUS;
                if (bonus < 0.5f) continue;
                RuneSpellRuntime.markEcho(target, bonus);
                SpellFx.damage(caster, target, "Elemental", bonus, null, null, cb);
            }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (!target.isValid()) return;
            try { EffectsManager.removeEffect(target, CURSE, cb); } catch (Exception e) { EldaniorLogger.error("MarqueFuneste: effet", e); }
        }
    }
}
