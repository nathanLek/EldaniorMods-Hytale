package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune du Croissant de Ki (Combat, Rare, Maitre Martial, voie martiale du Clan Luo) : un croissant de Ki geant (lame d'air
 * Tenryumaru x2,4, de la couleur du Ki du palier) file droit devant sur 20 blocs a 30 blocs/s et traverse tous les ennemis
 * (220 chacun, une fois). Un mur l'arrete.
 * Fiche de conception : scripts/runes/specs/Rune_CroissantKi.json
 */
public final class CroissantKi implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double SPEED = 30, RANGE = 20, HIT_RADIUS = 2.4;
    private static final float DAMAGE = 220f, SCALE = 2.4f;

    @Override public String id() { return "CroissantKi"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        int tier = MartialKi.tier(caster, store);
        return Tenryumaru.kiBlade(caster, pos, SpellFx.lookDirection(caster, store), MartialKi.bladeKey(tier),
                SPEED, RANGE, HIT_RADIUS, DAMAGE, SCALE, null, store, cb);
    }
}
