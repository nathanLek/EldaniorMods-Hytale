package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune du Halo Protecteur (Lumiere, Common) : halo de lumiere sur l'allie vise (groupe, guilde, famille), ou sur soi
 * sans allie sous le curseur : -30 % de degats subis pendant 8 s et 5 % de soin.
 * Fiche de conception : scripts/runes/specs/Rune_HaloProtecteur.json
 */
public final class HaloProtecteur implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 20.0;
    private static final float HEAL = 0.05f;

    // ===== Assets =====
    private static final String HALO = "Elda_HaloProtecteur";
    private static final String FX_LIGHT = "Bifrost_Divine";
    private static final String SFX_HALO = "SFX_Portal_Void";

    @Override public String id() { return "HaloProtecteur"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> aimed = SpellFx.targetLiving(caster, store, RANGE);
        Ref<EntityStore> target = aimed != null && SpellFx.isAlly(caster, aimed, store) ? aimed : caster;
        Vector3d p = SpellFx.position(target, store);
        if (p == null) return null;
        SpellFx.effect(target, HALO, cb);
        SpellFx.restore(target, DefaultEntityStatTypes.getHealth(), HEAL, cb);
        SpellFx.fx(FX_LIGHT, p.add(0, 1, 0), 0.5f, store, cb);
        SpellFx.sound(SFX_HALO, p, cb);
        return null;
    }
}
