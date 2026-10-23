package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de Bourrasque (Air, Common) : souffle de vent en cone qui repousse les ennemis proches et leur inflige de legers
 * degats. Sort instantane, sans modification du terrain.
 * Fiche de conception : scripts/runes/specs/Rune_Bourrasque.json
 */
public final class Bourrasque implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 8.0, HALF_ANGLE = Math.toRadians(35), MAX_HEIGHT = 3.0;
    private static final float DAMAGE = 40f;
    private static final double PUSH = 13.0, LIFT = 4.0;

    // ===== Assets =====
    private static final String FX_CANNON = "Fujin_Cannon", FX_GUST = "Fujin_Gust";
    private static final String SFX_GUST = "SFX_Ability_Wind_Strike_Slash";

    @Override public String id() { return "Bourrasque"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d look = SpellFx.lookDirection(caster, store), dir = SpellFx.forward(caster, store);
        float yaw = (float) Math.toDegrees(Math.atan2(look.x, look.z));
        float pitch = (float) Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, look.y))));
        Vector3d eye = new Vector3d(pos).add(0, 1.4, 0);

        SpellFx.fx(FX_CANNON, eye, yaw, pitch, 0.55f, store, cb);
        for (int i = 2; i <= RANGE; i += 3) SpellFx.fx(FX_GUST, new Vector3d(eye).add(new Vector3d(dir).mul(i)), 0.6f + i / 12f, store, cb);
        SpellFx.sound(SFX_GUST, pos, cb);

        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, RANGE + 1, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || Math.abs(p.y - pos.y) > MAX_HEIGHT) continue;
            Vector3d to = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
            double d = to.length();
            if (d > RANGE || (d > 0.5 && to.div(d).dot(dir) < Math.cos(HALF_ANGLE))) continue;
            Vector3d out = d > 0.5 ? new Vector3d(to).mul(PUSH) : new Vector3d(dir).mul(PUSH);
            SpellFx.damage(caster, e, "Wind", DAMAGE, out.add(0, LIFT, 0), "Impact_Light", cb);
            SpellFx.fx(FX_GUST, new Vector3d(p).add(0, 1, 0), 0.8f, store, cb);
        }
        return null;
    }
}
