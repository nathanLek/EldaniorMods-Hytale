package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune du Puits d'Ames (Tenebres, Rare) : puits de tenebres de 6 s au point vise (rayon 5). Les ennemis dedans sont
 * draines toutes les 0,5 s ; la moitie de ce qui est draine revient au lanceur en soin.
 * Fiche de conception : scripts/runes/specs/Rune_PuitsDAmes.json
 */
public final class PuitsDAmes implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, DRAIN_EVERY_MS = 500, MIST_EVERY_MS = 3_000, CORE_EVERY_MS = 600;
    private static final double TARGET_RANGE = 20.0, FALLBACK = 8.0, RADIUS = 5.0;
    private static final float DAMAGE = 10f, LIFESTEAL = 0.5f;
    private static final int MAX_ARCS = 4;                            // filets de drain visibles par passage

    // ===== Assets =====
    private static final String FX_MIST = "Nyx_Mist", FX_CORE = "Nyx_Void", FX_DRAIN = "Nyx_Drain_Burst";
    private static final String SFX_OPEN = "SFX_Bear_Voidtaken_Tentacle";

    @Override public String id() { return "PuitsDAmes"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d at = SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK);
        SpellFx.sound(SFX_OPEN, at, cb);
        return new Well(at, System.currentTimeMillis());
    }

    private static final class Well implements Instance {
        final Vector3d at;
        final long startAt;
        long lastDrain, lastMist = Long.MIN_VALUE / 2, lastCore;

        Well(Vector3d at, long startAt) { this.at = at; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastMist >= MIST_EVERY_MS) {
                lastMist = now;
                for (double dx = -RADIUS + 1.5; dx <= RADIUS - 1.5; dx += 2.5)
                    for (double dz = -RADIUS + 1.5; dz <= RADIUS - 1.5; dz += 2.5)
                        SpellFx.fx(FX_MIST, new Vector3d(at).add(dx, 0.1, dz), 0.6f, store, cb);
            }
            if (now - lastCore >= CORE_EVERY_MS) { lastCore = now; SpellFx.fx(FX_CORE, at, 1.0f, store, cb); }
            if (now - lastDrain < DRAIN_EVERY_MS) return false;
            lastDrain = now;
            Vector3d cp = SpellFx.position(caster, store);
            float drained = 0;
            int arcs = 0;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, RADIUS + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, at) > RADIUS || Math.abs(p.y - at.y) > 3) continue;
                SpellFx.damage(caster, e, "Poison", DAMAGE, null, null, cb);
                drained += DAMAGE;
                if (cp != null && arcs++ < MAX_ARCS) SpellFx.arc(FX_DRAIN, p.add(0, 1, 0), new Vector3d(cp).add(0, 1.2, 0), 0.4f, store, cb);
            }
            SpellFx.heal(caster, drained * LIFESTEAL, cb);
            return false;
        }
    }
}
