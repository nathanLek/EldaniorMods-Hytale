package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de la Paume de Ki (Combat, Common, Apprenti en Arts Martiaux, voie martiale du Clan Luo) : le Ki concentre dans la
 * paume frappe le premier ennemi a 3 blocs devant le lanceur (vise, sinon le plus proche devant) : 70 degats, gros recul,
 * etourdi 0,5 s. Marche sans arme.
 * Fiche de conception : scripts/runes/specs/Rune_PaumeKi.json
 */
public final class PaumeKi implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 3.5, PUSH = 10.0, LIFT = 3.0;
    private static final float DAMAGE = 70f;
    private static final long STUN_MS = 500;

    // ===== Assets =====
    private static final String FX_HIT = "Impact_Sword_Charged", FX_RING = "Eldanior_Pas_Celeste", STUN = "Stun";
    private static final String SFX_HIT = "SFX_Sword_T2_Impact";

    @Override public String id() { return "PaumeKi"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d fwd = SpellFx.forward(caster, store);
        fwd.y = 0;
        if (fwd.lengthSquared() < 1e-4) fwd.set(0, 0, 1);
        fwd.normalize();
        Vector3d palm = new Vector3d(pos).add(0, 1.2, 0).add(new Vector3d(fwd).mul(1.2));
        SpellFx.fx(FX_RING, palm, (float) Math.toDegrees(Math.atan2(fwd.x, fwd.z)), 90f, 0.7f, store, cb);
        Ref<EntityStore> target = SpellFx.targetEntity(caster, store, RANGE);
        if (target == null) {                                                  // le plus proche devant soi
            double best = Double.MAX_VALUE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, new Vector3d(pos).add(new Vector3d(fwd).mul(1.5)), 2.0, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                double d = p.distance(pos);
                if (d < best) { best = d; target = e; }
            }
        }
        if (target == null) return null;
        Vector3d p = SpellFx.position(target, store);
        if (p == null) return null;
        Vector3d chest = new Vector3d(p).add(0, 1.1, 0);
        SpellFx.fx(FX_HIT, chest, 1.3f, store, cb);
        SpellFx.sound(SFX_HIT, chest, cb);
        SpellFx.damage(caster, target, "Physical", DAMAGE, new Vector3d(fwd).mul(PUSH).add(0, LIFT, 0), "Impact_Strong", cb);
        SpellFx.stun(target, STUN, STUN_MS, store, cb);
        return null;
    }
}
