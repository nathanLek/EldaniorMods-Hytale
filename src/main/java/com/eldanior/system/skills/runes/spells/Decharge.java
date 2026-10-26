package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de Décharge (Foudre, Common) : eclair instantane sur l'ennemi vise qui l'etourdit une demi-seconde.
 * Sans cible, l'arc part dans le vide (effet visuel seul).
 * Fiche de conception : scripts/runes/specs/Rune_Decharge.json
 */
public final class Decharge implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 18.0;
    private static final float DAMAGE = 55f;
    private static final long STUN_MS = 500;

    // ===== Assets =====
    private static final String STUN = "Taranis_Foudroye";
    private static final String FX_SPARK = "Taranis_Electric_Burst";
    private static final String SFX_ZAP = "SFX_Emit_Goblin_Electric_Fence_Zap";

    @Override public String id() { return "Decharge"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d hand = new Vector3d(pos).add(0, 1.4, 0).add(SpellFx.forward(caster, store).mul(0.6));
        Ref<EntityStore> target = SpellFx.targetEntity(caster, store, RANGE);
        Vector3d tp = SpellFx.position(target, store);
        Vector3d end = tp != null ? tp.add(0, 1, 0) : new Vector3d(hand).add(SpellFx.lookDirection(caster, store).mul(RANGE * 0.6));

        SpellFx.arc(FX_SPARK, hand, end, 0.4f, store, cb);
        SpellFx.sound(SFX_ZAP, end, cb);
        if (tp == null) return null;
        SpellFx.fx(FX_SPARK, end, 1.0f, store, cb);
        SpellFx.damage(caster, target, "Lightning", DAMAGE, null, "Impact_Light", cb);
        SpellFx.stun(target, STUN, STUN_MS, store, cb);
        return null;
    }
}
