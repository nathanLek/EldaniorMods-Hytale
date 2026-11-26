package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.skills.system.InvisibilityManager;
import com.eldanior.system.util.NpcAttitudes;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Rune du Silence du Neant (Tenebres, Unique, rune de predilection du Neant Mystique) : domaine fixe de 10 blocs autour du
 * lanceur pendant 10 s. Monstres dedans : attitude IGNORE forcee envers tous les joueurs proches et cible verrouillee
 * effacee (rafraichi toutes les 0,5 s) : ils n'attaquent plus. Joueurs ennemis dedans : Elda_Silence et runes dissipees
 * (RuneSpellRuntime.silence). Tous : 12,5 degats / 0,5 s. Le lanceur est invisible (InvisibilityManager, totale) tant
 * qu'il reste dans le domaine.
 * Fiche de conception : scripts/runes/specs/Rune_SilenceNeant.json
 */
public final class SilenceNeant implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, PULSE_EVERY_MS = 500, MIST_EVERY_MS = 3_000, VOID_FX_EVERY_MS = 1_000;
    private static final long SILENCE_MS = 1_000, TINT_MS = 2_000;
    private static final double RADIUS = 10.0, HEIGHT = 6.0, WITNESS_RADIUS = 14.0;
    private static final float DAMAGE = 12.5f;

    // ===== Assets =====
    private static final String SILENCE = "Elda_Silence";
    private static final String FX_MIST = "Nyx_Mist", FX_VOID = "Nyx_Void";
    private static final String SFX_VOID = "SFX_Portal_Void";

    @Override public String id() { return "SilenceNeant"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.sound(SFX_VOID, pos, cb);
        Hush h = new Hush(caster, new Vector3d(pos), SpellFx.uuid(caster, store), System.currentTimeMillis());
        h.mist(store, cb);
        return h;
    }

    private static final class Hush implements Instance {
        final Ref<EntityStore> caster;
        final Vector3d center;
        final UUID uuid;
        final long startAt;
        final Map<Ref<EntityStore>, Long> tintedUntil = new HashMap<>();
        boolean hidden;
        long lastPulse, lastMist, lastVoidFx;

        Hush(Ref<EntityStore> caster, Vector3d center, UUID uuid, long startAt) {
            this.caster = caster; this.center = center; this.uuid = uuid; this.startAt = startAt; this.lastMist = startAt; this.lastVoidFx = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d me = SpellFx.position(caster, store);
            if (me == null || now - startAt >= DURATION_MS) return true;
            boolean inside = inside(me);
            if (inside != hidden && uuid != null) {                               // invisible dans le silence seulement
                hidden = inside;
                if (inside) InvisibilityManager.applyInvisibility(uuid, caster, InvisibilityManager.InvisType.TOTAL, (DURATION_MS - (now - startAt)) / 1000f, cb);
                else InvisibilityManager.removeInvisibility(uuid, caster, cb);
            }
            if (now - lastMist >= MIST_EVERY_MS) { lastMist = now; mist(store, cb); }
            if (now - lastPulse < PULSE_EVERY_MS) return false;
            lastPulse = now;
            boolean fx = now - lastVoidFx >= VOID_FX_EVERY_MS;
            if (fx) lastVoidFx = now;
            List<Ref<EntityStore>> witnesses = SpellFx.playersNear(center, WITNESS_RADIUS + RADIUS, store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + HEIGHT, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || !inside(p) || RuneSpellRuntime.isSummon(e)) continue;
                if (store.getComponent(e, Player.getComponentType()) != null) {
                    RuneSpellRuntime.silence(SpellFx.uuid(e, store), now + SILENCE_MS);
                    Long until = tintedUntil.get(e);
                    if (until == null || until - now < PULSE_EVERY_MS * 2) {      // renouvele avant expiration : pas de clignotement
                        EffectsManager.applyCustomEffect(e, SILENCE, TINT_MS / 1000f, OverlapBehavior.OVERWRITE, cb);
                        tintedUntil.put(e, now + TINT_MS);
                    }
                } else {
                    for (Ref<EntityStore> w : witnesses) NpcAttitudes.force(e, w, Attitude.IGNORE, (PULSE_EVERY_MS + 300) / 1000.0, store);
                    CloneFx.mark(e, "LockedTarget", null, store);
                }
                SpellFx.damage(caster, e, "Elemental", DAMAGE, null, null, cb);
                if (fx) SpellFx.fx(FX_VOID, new Vector3d(p).add(0, 0.3, 0), 0.6f, store, cb);
            }
            return false;
        }

        private boolean inside(Vector3d p) {
            return SpellFx.horizontalDistance(p, center) <= RADIUS && Math.abs(p.y - center.y) <= HEIGHT;
        }

        /** Brume du neant au sol : centre et anneau de 6 a mi-rayon. */
        void mist(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.fx(FX_MIST, center, 2.0f, store, cb);
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3;
                SpellFx.fx(FX_MIST, new Vector3d(center).add(Math.cos(a) * RADIUS * 0.6, 0, Math.sin(a) * RADIUS * 0.6), 1.6f, store, cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (hidden && uuid != null && caster.isValid()) InvisibilityManager.removeInvisibility(uuid, caster, cb);
        }
    }
}
