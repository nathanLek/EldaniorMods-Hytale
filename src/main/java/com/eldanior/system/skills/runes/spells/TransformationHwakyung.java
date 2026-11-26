package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune de la Transformation Hwakyung (Combat, Legendaire, Maitre Supreme, voie martiale du Clan Luo) : 20 s de corps de Ki
 * (effet Hwakyung_Corps) : +35 % de degats infliges (CombatStatsSystem -> damageMultiplier), -30 % de degats subis, malus
 * purges toutes les 0,25 s (etourdissements, ralentissements : insensible aux controles), et chaque coup libere une onde de
 * Ki autour de la cible (40 % du coup, rayon 3 ; les coups de l'onde ne relancent pas d'onde).
 * Fiche de conception : scripts/runes/specs/Rune_TransformationHwakyung.json
 */
public final class TransformationHwakyung implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 20_000, REFRESH_MS = 1_000, CLEANSE_MS = 250;
    private static final float DAMAGE_BONUS = 0.35f, REDUCTION = 0.30f, WAVE = 0.40f;
    private static final double WAVE_RADIUS = 3.0;

    // ===== Assets =====
    private static final String EFFECT = "Hwakyung_Corps", FX_WAVE = "Eldanior_Pas_Celeste", FX_BURST = "Gilgamesh_Sword_Burst";
    private static final String SFX_START = "SFX_Ability_Wind_Strike_Imbue", SFX_WAVE = "SFX_Sword_T2_Impact";

    @Override public String id() { return "TransformationHwakyung"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.effect(caster, EFFECT, cb);
        SpellFx.fx(FX_WAVE, new Vector3d(pos).add(0, 0.1, 0), 3.0f, store, cb);
        SpellFx.fx(FX_BURST, new Vector3d(pos).add(0, 1.2, 0), 2.0f, store, cb);
        SpellFx.sound(SFX_START, pos, cb);
        SpellFx.shake(pos, 12, "Impact_Strong", store);
        long now = System.currentTimeMillis();
        return new Body(caster, now);
    }

    /** Multiplicateur de degats de la transformation (CombatStatsSystem), 1 sans elle. */
    public static float damageMultiplier(Ref<EntityStore> attacker, Store<EntityStore> store) {
        if (attacker == null || !attacker.isValid()) return 1f;
        Body b = RuneSpellRuntime.find(SpellFx.uuid(attacker, store), Body.class);
        return b != null ? 1f + DAMAGE_BONUS : 1f;
    }

    static final class Body implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        final Queue<Object[]> hits = new ConcurrentLinkedQueue<>();
        final Map<Ref<EntityStore>, Long> own = new ConcurrentHashMap<>();
        long lastRefresh, lastCleanse;

        Body(Ref<EntityStore> caster, long now) { this.caster = caster; this.startAt = now; this.lastRefresh = now; }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            Long until = own.get(target);
            if (until != null && until > System.currentTimeMillis()) return;    // coup de l'onde elle-meme
            if (hits.size() < 32) hits.add(new Object[]{target, amount});
        }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            return amount * (1f - REDUCTION);
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastRefresh >= REFRESH_MS) { lastRefresh = now; SpellFx.effect(caster, EFFECT, cb); own.values().removeIf(t -> t < now); }
            if (now - lastCleanse >= CLEANSE_MS) {
                lastCleanse = now;
                try { EffectsManager.cureAllDebuffs(caster, cb); } catch (Exception e) { EldaniorLogger.error("Hwakyung: purge", e); }
            }
            for (Object[] h; (h = hits.poll()) != null; ) {
                @SuppressWarnings("unchecked") Ref<EntityStore> target = (Ref<EntityStore>) h[0];
                Vector3d p = target != null && target.isValid() ? SpellFx.position(target, store) : null;
                if (p == null) continue;
                float wave = (float) h[1] * WAVE;
                SpellFx.fx(FX_WAVE, new Vector3d(p).add(0, 0.2, 0), 1.2f, store, cb);
                SpellFx.sound(SFX_WAVE, p, cb);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, p, WAVE_RADIUS, store))
                    MartialKi.kiDamage(caster, e, wave, "Physical", null, own, cb);
            }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (caster == null || !caster.isValid()) return;
            try { EffectsManager.removeEffect(caster, EFFECT, cb); } catch (Exception ignored) { }
        }
    }
}
