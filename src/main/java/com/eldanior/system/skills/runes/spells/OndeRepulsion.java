package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune d'Onde de Répulsion (Spatial, Common) : onde de gravite inversee qui repousse tous les ennemis autour du lanceur.
 * Sort instantane, radial (contrairement a la Bourrasque, en cone), sans modification du terrain.
 * Fiche de conception : scripts/runes/specs/Rune_OndeRepulsion.json
 */
public final class OndeRepulsion implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RADIUS = 6.0, MAX_HEIGHT = 3.0;
    private static final float DAMAGE = 30f;
    private static final double PUSH = 15.0, LIFT = 6.0;

    // ===== Assets =====
    private static final String FX_WAVE = "Chaos_Shockwave", FX_BURST = "Chaos_Burst";
    private static final String SFX_WAVE = "SFX_Portal_Void";

    @Override public String id() { return "OndeRepulsion"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d dir = SpellFx.forward(caster, store);

        SpellFx.fx(FX_WAVE, new Vector3d(pos).add(0, 0.2, 0), 1.2f, store, cb);
        SpellFx.fx(FX_BURST, new Vector3d(pos).add(0, 1, 0), 1.0f, store, cb);
        SpellFx.sound(SFX_WAVE, pos, cb);

        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, RADIUS + 1, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || Math.abs(p.y - pos.y) > MAX_HEIGHT) continue;
            Vector3d to = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
            double d = to.length();
            if (d > RADIUS) continue;
            Vector3d out = d > 0.5 ? to.div(d).mul(PUSH) : new Vector3d(dir).mul(PUSH);
            SpellFx.damage(caster, e, "Crush", DAMAGE, out.add(0, LIFT, 0), "Impact_Light", cb);
            SpellFx.fx(FX_BURST, new Vector3d(p).add(0, 1, 0), 0.6f, store, cb);
        }
        return null;
    }
}
