package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune du Decret (Spatial, Epic, rune de predilection de l'Archimage Royal) : les ennemis a 8 blocs du point vise (20 blocs,
 * 12 au plus) sont plaques au sol (poussee vers le bas, puis figes 2 s : lockInPlace + Elda_Decret), 40 degats ;
 * puis la gravite s'inverse et les ejecte vers le haut et l'exterieur, 150 degats.
 * Fiche de conception : scripts/runes/specs/Rune_Decret.json
 */
public final class Decret implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long LOCK_AFTER_MS = 250, PINNED_MS = 2_000, AURA_EVERY_MS = 300;
    private static final double RANGE = 20.0, RADIUS = 8.0, SLAM = 18.0, EJECT_OUT = 10.0, EJECT_UP = 16.0;
    private static final int MAX_TARGETS = 12;
    private static final float SLAM_DAMAGE = 40f, EJECT_DAMAGE = 150f;

    // ===== Assets =====
    private static final String PINNED = "Elda_Decret";
    private static final String FX_WAVE = "Chaos_Shockwave", FX_AURA = "Telekinesie_Aura", FX_GUST = "Fujin_Gust";
    private static final String SFX_SLAM = "SFX_Golem_Earth_Slam_Impact", SFX_EJECT = "SFX_Ability_Wind_Strike_Slash";

    @Override public String id() { return "Decret"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        List<Ref<EntityStore>> pinned = new ArrayList<>();
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS, store)) {
            if (pinned.size() >= MAX_TARGETS) break;
            if (RuneSpellRuntime.isSummon(e)) continue;
            pinned.add(e);
            SpellFx.damage(caster, e, "Physical", SLAM_DAMAGE, new Vector3d(0, -SLAM, 0), "Impact_Strong", cb);   // plaques au sol
            EffectsManager.applyCustomEffect(e, PINNED, (PINNED_MS + LOCK_AFTER_MS) / 1000f, OverlapBehavior.OVERWRITE, cb);
        }
        SpellFx.fx(FX_WAVE, new Vector3d(center).add(0, 0.3, 0), (float) (RADIUS / 4.0), store, cb);
        SpellFx.sound(SFX_SLAM, center, cb);
        SpellFx.shake(center, RADIUS * 2, "Impact_Strong", store);
        return new Decree(center, pinned, System.currentTimeMillis());
    }

    private static final class Decree implements Instance {
        final Vector3d center;
        final List<Ref<EntityStore>> pinned;
        final long startAt;
        boolean locked;
        long lastAura;

        Decree(Vector3d center, List<Ref<EntityStore>> pinned, long startAt) { this.center = center; this.pinned = pinned; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            long t = now - startAt;
            pinned.removeIf(e -> SpellFx.position(e, store) == null);
            if (pinned.isEmpty() || SpellFx.position(caster, store) == null) return true;
            if (!locked && t >= LOCK_AFTER_MS) {                                // une fois au sol, ils ne bougent plus
                locked = true;
                for (Ref<EntityStore> e : pinned) SpellFx.lockInPlace(e, PINNED_MS, store);
            }
            if (t >= LOCK_AFTER_MS + PINNED_MS) { eject(caster, store, cb); return true; }
            if (now - lastAura >= AURA_EVERY_MS) {
                lastAura = now;
                for (Ref<EntityStore> e : pinned) { Vector3d p = SpellFx.position(e, store); if (p != null) SpellFx.fx(FX_AURA, new Vector3d(p).add(0, 0.8, 0), 0.8f, store, cb); }
            }
            return false;
        }

        /** La gravite s'inverse : ejection vers le haut et l'exterieur. */
        private void eject(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.fx(FX_WAVE, new Vector3d(center).add(0, 0.3, 0), (float) (RADIUS / 3.0), store, cb);
            SpellFx.fx(FX_GUST, new Vector3d(center).add(0, 1, 0), 2.0f, store, cb);
            SpellFx.sound(SFX_EJECT, center, cb);
            for (Ref<EntityStore> e : pinned) {
                SpellFx.unlock(e);
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                try { EffectsManager.removeEffect(e, PINNED, cb); } catch (Exception ex) { EldaniorLogger.error("Decret: effet", ex); }
                Vector3d away = new Vector3d(p.x - center.x, 0, p.z - center.z);
                if (away.lengthSquared() < 1e-6) away.set(1, 0, 0);
                SpellFx.damage(caster, e, "Physical", EJECT_DAMAGE, away.normalize().mul(EJECT_OUT).add(0, EJECT_UP, 0), "Impact_Strong", cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { for (Ref<EntityStore> e : pinned) SpellFx.unlock(e); }

        @Override
        public void abort() { for (Ref<EntityStore> e : pinned) SpellFx.unlock(e); }
    }
}
