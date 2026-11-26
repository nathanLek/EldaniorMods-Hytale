package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Rune de Transformation d'Arme (Combat, Legendaire, Mousquetaire) : transforme pendant 5 minutes le mousquet legendaire
 * tenu (Black Bess -> SARS, Green Colt -> Colts de cavalerie, Blue Tiger -> canon de char a main), puis l'arme redevient
 * le mousquet (ArmeTransfo). La garde Eldanior_ArmeLegendaireTenue a deja verifie l'arme tenue avant le cout.
 * Fiche de conception : scripts/runes/specs/Rune_TransformationArme.json
 */
public final class TransformationArme implements RuneSpell {

    @Override public String id() { return "TransformationArme"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        ArmeTransfo.transform(caster, store, cb);
        return null;
    }
}
