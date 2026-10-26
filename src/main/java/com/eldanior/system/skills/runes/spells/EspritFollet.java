package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de l'Esprit Follet (Lumiere, Common) : un esprit de lumiere (particules) flotte a l'epaule du lanceur et tire
 * 5 traits, un toutes les 1,2 s, sur l'ennemi le plus proche. Pas de PNJ : l'esprit n'est qu'un visuel.
 * Fiche de conception : scripts/runes/specs/Rune_EspritFollet.json
 */
public final class EspritFollet implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long SHOT_EVERY_MS = 1_200, GLOW_EVERY_MS = 100, MAX_MS = 10_000;
    private static final int SHOTS = 5;
    private static final double RANGE = 15.0;
    private static final float DAMAGE = 16f;

    // ===== Assets =====
    private static final String FX_WISP = "Bifrost_Divine";
    private static final String SFX_SHOT = "SFX_Arrow_Frost_Hit";

    @Override public String id() { return "EspritFollet"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        long now = System.currentTimeMillis();
        return new Wisp(caster, now);
    }

    private static final class Wisp implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        long lastShot, lastGlow;
        int shots;

        Wisp(Ref<EntityStore> caster, long startAt) { this.caster = caster; this.startAt = startAt; this.lastShot = startAt - SHOT_EVERY_MS / 2; }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (shots >= SHOTS || now - startAt >= MAX_MS) return true;
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            Vector3d fwd = SpellFx.forward(caster, store);
            double bob = Math.sin((now - startAt) / 250.0) * 0.15;
            Vector3d wisp = new Vector3d(pos).add(-fwd.z * 0.7, 2.1 + bob, fwd.x * 0.7);  // au-dessus de l'epaule droite
            if (now - lastGlow >= GLOW_EVERY_MS) { lastGlow = now; SpellFx.fx(FX_WISP, wisp, 0.25f, store, cb); }
            if (now - lastShot < SHOT_EVERY_MS) return false;
            Ref<EntityStore> target = nearest(wisp, store);
            if (target == null) return false;
            lastShot = now;
            shots++;
            Vector3d tp = SpellFx.position(target, store);
            if (tp == null) return false;
            tp.add(0, 1, 0);
            SpellFx.arc(FX_WISP, wisp, tp, 0.25f, store, cb);
            SpellFx.sound(SFX_SHOT, tp, cb);
            SpellFx.damage(caster, target, "Elemental", DAMAGE, null, null, cb);
            return false;
        }

        private Ref<EntityStore> nearest(Vector3d from, Store<EntityStore> store) {
            Ref<EntityStore> best = null;
            double bestD = RANGE * RANGE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, from, RANGE, store)) {
                if (RuneSpellRuntime.isSummon(e) || !CloneFx.isMonster(e, store)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                double d = p.distanceSquared(from);
                if (d < bestD) { bestD = d; best = e; }
            }
            return best;
        }
    }
}
