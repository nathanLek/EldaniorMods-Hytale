package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune de l'Arme Benie (Lumiere, Rare) : 10 s pendant lesquelles chaque coup du lanceur (sorts compris) declenche un
 * eclat sacre sur la cible et soigne un peu le lanceur (4 eclats par seconde au plus). Les eclats sont marques "echo"
 * pour ne pas se redeclencher eux-memes.
 * Fiche de conception : scripts/runes/specs/Rune_ArmeBenie.json
 */
public final class ArmeBenie implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, BURST_EVERY_MS = 250;
    private static final float BONUS_DAMAGE = 25f, HEAL = 0.02f;

    // ===== Assets =====
    private static final String BLESSED = "Elda_ArmeBenie";
    private static final String FX_BURST = "Bifrost_Divine";
    private static final String SFX_CAST = "SFX_Portal_Void";

    @Override public String id() { return "ArmeBenie"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.effect(caster, BLESSED, cb);
        SpellFx.fx(FX_BURST, new Vector3d(pos).add(0, 1.2, 0), 0.6f, store, cb);
        SpellFx.sound(SFX_CAST, pos, cb);
        return new Blessing(caster, System.currentTimeMillis());
    }

    private static final class Blessing implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        final Queue<Ref<EntityStore>> hits = new ConcurrentLinkedQueue<>();   // coups recus pendant le traitement des degats
        long lastBurst;

        Blessing(Ref<EntityStore> caster, long startAt) { this.caster = caster; this.startAt = startAt; }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            if (hits.size() < 8) hits.add(target);                                // memorise seulement (applique dans tick)
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            Ref<EntityStore> target = hits.poll();
            if (target == null || now - lastBurst < BURST_EVERY_MS) return false;
            hits.clear();
            if (!target.isValid() || target.equals(caster)) return false;
            lastBurst = now;
            Vector3d p = SpellFx.position(target, store);
            if (p != null) SpellFx.fx(FX_BURST, p.add(0, 1, 0), 0.35f, store, cb);
            RuneSpellRuntime.markEcho(target, BONUS_DAMAGE);
            SpellFx.damage(caster, target, "Elemental", BONUS_DAMAGE, null, null, cb);
            SpellFx.restore(caster, DefaultEntityStatTypes.getHealth(), HEAL, cb);
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (!caster.isValid()) return;
            try { EffectsManager.removeEffect(caster, BLESSED, cb); } catch (Exception e) { EldaniorLogger.error("ArmeBenie: effet", e); }
        }
    }
}
