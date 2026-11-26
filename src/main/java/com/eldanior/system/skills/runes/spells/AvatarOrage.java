package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune de l'Avatar de l'Orage (Foudre, Unique, rune de predilection de l'Archi-Magus) : 15 s pendant lesquelles chaque
 * coup du lanceur fait tomber un eclair sur la cible (60 + etourdissement 0,3 s), qui saute sur l'ennemi le plus proche a
 * 6 blocs (40). 3 eclairs par seconde au plus ; les degats des eclairs sont marques (markEcho) pour ne pas se relancer.
 * Fiche de conception : scripts/runes/specs/Rune_AvatarOrage.json
 */
public final class AvatarOrage implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 15_000, BOLT_GAP_MS = 333, STUN_MS = 300, AURA_EVERY_MS = 1_000;
    private static final double JUMP = 6.0;
    private static final float BOLT = 60f, JUMP_DAMAGE = 40f;

    // ===== Assets =====
    private static final String STORM = "Elda_AvatarOrage", STUN = "Taranis_Foudroye";
    private static final String FX_BOLT = "Taranis_Bolt", FX_SPARK = "Taranis_Electric_Burst";
    private static final String SFX_ZAP = "SFX_Emit_Goblin_Electric_Fence_Zap";

    @Override public String id() { return "AvatarOrage"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.effect(caster, STORM, cb);
        SpellFx.fx(FX_BOLT, pos, 1.0f, store, cb);
        SpellFx.sound(SFX_ZAP, pos, cb);
        return new Storm(caster, System.currentTimeMillis());
    }

    private static final class Storm implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        final Queue<Ref<EntityStore>> struck = new ConcurrentLinkedQueue<>();
        volatile long lastQueued;
        long lastAura;

        Storm(Ref<EntityStore> caster, long startAt) { this.caster = caster; this.startAt = startAt; }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            long now = System.currentTimeMillis();
            if (amount <= 0 || now - lastQueued < BOLT_GAP_MS) return;
            lastQueued = now;
            struck.add(target);
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null || now - startAt >= DURATION_MS) return true;
            Ref<EntityStore> t;
            while ((t = struck.poll()) != null) bolt(t, store, cb);
            if (now - lastAura >= AURA_EVERY_MS) { lastAura = now; SpellFx.fx(FX_SPARK, new Vector3d(pos).add(0, 1.2, 0), 0.7f, store, cb); }
            return false;
        }

        /** Eclair sur la cible, puis saut vers l'ennemi le plus proche. */
        private void bolt(Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d p = SpellFx.position(target, store);
            if (p == null) return;
            RuneSpellRuntime.markEcho(target, BOLT);
            SpellFx.damage(caster, target, "Lightning", BOLT, null, "Impact_Light", cb);
            SpellFx.stun(target, STUN, STUN_MS, store, cb);
            SpellFx.fx(FX_BOLT, p, 0.8f, store, cb);
            SpellFx.sound(SFX_ZAP, p, cb);
            Ref<EntityStore> next = null;
            double best = Double.MAX_VALUE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, p, JUMP, store)) {
                if (e.equals(target)) continue;
                Vector3d q = SpellFx.position(e, store);
                if (q != null && q.distance(p) < best) { best = q.distance(p); next = e; }
            }
            if (next == null) return;
            Vector3d q = SpellFx.position(next, store);
            SpellFx.arc(FX_SPARK, new Vector3d(p).add(0, 1, 0), new Vector3d(q).add(0, 1, 0), 0.4f, store, cb);
            RuneSpellRuntime.markEcho(next, JUMP_DAMAGE);
            SpellFx.damage(caster, next, "Lightning", JUMP_DAMAGE, null, null, cb);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (!caster.isValid()) return;
            try { EffectsManager.removeEffect(caster, STORM, cb); } catch (Exception e) { EldaniorLogger.error("AvatarOrage: effet", e); }
        }
    }
}
