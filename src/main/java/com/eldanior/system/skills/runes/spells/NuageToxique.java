package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

/**
 * Rune du Nuage Toxique (Tenebres, Epic, rune de predilection du Grand Alchimiste) : nuage de poison de 10 s (rayon 6) au
 * point vise (20 blocs), qui derive a 2 b/s vers le point que vise le lanceur. Ennemis dedans : 14 degats de poison toutes
 * les 0,5 s et teinte Elda_Toxique (renouvelee juste avant d'expirer, pour ne pas clignoter).
 * Fiche de conception : scripts/runes/specs/Rune_NuageToxique.json
 */
public final class NuageToxique implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, HIT_EVERY_MS = 500, CLOUD_EVERY_MS = 1_000, TINT_MS = 2_000;
    private static final double RANGE = 20.0, RADIUS = 6.0, DRIFT = 2.0;
    private static final float DAMAGE = 14f;

    // ===== Assets =====
    private static final String TOXIC = "Elda_Toxique";
    private static final String FX_CLOUD = "Nuage_Toxique";
    private static final String SFX_OPEN = "SFX_Bomb_Fuse";

    @Override public String id() { return "NuageToxique"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        SpellFx.fx(FX_CLOUD, center, 1.0f, store, cb);
        SpellFx.sound(SFX_OPEN, center, cb);
        return new Cloud(center, System.currentTimeMillis());
    }

    private static final class Cloud implements Instance {
        final Vector3d center;
        final long startAt;
        final Map<Ref<EntityStore>, Long> tintedUntil = new HashMap<>();
        long lastTick, lastHit, lastCloud;

        Cloud(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; this.lastTick = startAt; this.lastCloud = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null || now - startAt >= DURATION_MS) return true;
            double dt = Math.min(0.2, (now - lastTick) / 1000.0);
            lastTick = now;
            Vector3d aim = SpellFx.targetPoint(caster, store, RANGE, RANGE);   // le nuage derive vers le curseur
            Vector3d delta = new Vector3d(aim).sub(center);
            if (delta.length() > DRIFT * dt) delta.normalize().mul(DRIFT * dt);
            center.add(delta);
            if (now - lastCloud >= CLOUD_EVERY_MS) { lastCloud = now; SpellFx.fx(FX_CLOUD, center, 1.0f, store, cb); }
            if (now - lastHit >= HIT_EVERY_MS) {
                lastHit = now;
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + 2, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || SpellFx.horizontalDistance(p, center) > RADIUS || Math.abs(p.y - center.y) > 3.5) continue;
                    SpellFx.damage(caster, e, "Poison", DAMAGE, null, null, cb);
                    Long until = tintedUntil.get(e);
                    if (until == null || until - now < HIT_EVERY_MS * 2) {      // renouvelee avant expiration : pas de clignotement
                        EffectsManager.applyCustomEffect(e, TOXIC, TINT_MS / 1000f, OverlapBehavior.OVERWRITE, cb);
                        tintedUntil.put(e, now + TINT_MS);
                    }
                }
            }
            return false;
        }
    }
}
