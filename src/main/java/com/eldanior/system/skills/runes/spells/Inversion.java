package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

/**
 * Rune de l'Inversion (Spatial, Epic, rune de predilection du Maitre du Prodige) : gravite inversee 6 s sur la zone visee
 * (rayon 7, 20 blocs), en 3 cycles de 2 s. Pendant 1,6 s les ennemis (12 au plus) s'elevent et flottent a 4 blocs de leur
 * sol (vitesse verticale imposee, horizontale annulee), puis la gravite revient d'un coup : plaques au sol, 80 degats.
 * Fiche de conception : scripts/runes/specs/Rune_Inversion.json
 */
public final class Inversion implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long CYCLE_MS = 2_000, LIFT_MS = 1_600, AURA_EVERY_MS = 300, VORTEX_EVERY_MS = 1_000;
    private static final int CYCLES = 3, MAX_TARGETS = 12;
    private static final double RANGE = 20.0, RADIUS = 7.0, HOVER = 4.0, RISE_GAIN = 5.0, RISE_MAX = 9.0, SLAM = 25.0;
    private static final float DAMAGE = 80f;

    // ===== Assets =====
    private static final String FX_AURA = "Telekinesie_Aura", FX_VORTEX = "Chaos_Vortex", FX_SLAM = "Chaos_Shockwave";
    private static final String SFX_OPEN = "SFX_Portal_Void", SFX_SLAM = "SFX_Golem_Earth_Slam_Impact";

    @Override public String id() { return "Inversion"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d center = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        SpellFx.fx(FX_VORTEX, new Vector3d(center).add(0, 1, 0), 1.5f, store, cb);
        SpellFx.sound(SFX_OPEN, center, cb);
        return new Field(center, System.currentTimeMillis());
    }

    private static final class Field implements Instance {
        final Vector3d center;
        final long startAt;
        final Map<Ref<EntityStore>, Double> floating = new HashMap<>();        // souleve -> hauteur de son sol
        int cycle = -1;
        boolean slammed;
        long lastAura, lastVortex;

        Field(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            long t = now - startAt;
            int c = (int) (t / CYCLE_MS);
            if (c >= CYCLES) return true;
            if (c != cycle) { cycle = c; slammed = false; capture(caster, store); }
            if (t - c * CYCLE_MS < LIFT_MS) levitate(now, store, cb);
            else if (!slammed) { slammed = true; slam(caster, store, cb); }
            if (now - lastVortex >= VORTEX_EVERY_MS) { lastVortex = now; SpellFx.fx(FX_VORTEX, new Vector3d(center).add(0, 1.5, 0), 1.2f, store, cb); }
            return false;
        }

        private void capture(Ref<EntityStore> caster, Store<EntityStore> store) {
            floating.clear();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + 2, store)) {
                if (floating.size() >= MAX_TARGETS || RuneSpellRuntime.isSummon(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p != null && SpellFx.horizontalDistance(p, center) <= RADIUS) floating.put(e, p.y);
            }
        }

        /** Les souleves montent vers leur sol + 4 blocs et y flottent, sans derive horizontale. */
        private void levitate(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            boolean aura = now - lastAura >= AURA_EVERY_MS;
            if (aura) lastAura = now;
            for (Map.Entry<Ref<EntityStore>, Double> en : floating.entrySet()) {
                Vector3d p = SpellFx.position(en.getKey(), store);
                if (p == null) continue;
                double vy = Math.max(-RISE_MAX, Math.min(RISE_MAX, (en.getValue() + HOVER - p.y) * RISE_GAIN));
                SpellFx.push(en.getKey(), new Vector3d(0, vy, 0), false, cb);
                if (aura) SpellFx.fx(FX_AURA, new Vector3d(p).add(0, 0.8, 0), 0.7f, store, cb);
            }
        }

        /** La gravite revient : tous plaques au sol. */
        private void slam(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.fx(FX_SLAM, new Vector3d(center).add(0, 0.3, 0), (float) (RADIUS / 4.0), store, cb);
            SpellFx.sound(SFX_SLAM, center, cb);
            for (Ref<EntityStore> e : floating.keySet()) {
                if (SpellFx.position(e, store) == null) continue;
                SpellFx.damage(caster, e, "Physical", DAMAGE, new Vector3d(0, -SLAM, 0), "Impact_Strong", cb);
            }
        }
    }
}
