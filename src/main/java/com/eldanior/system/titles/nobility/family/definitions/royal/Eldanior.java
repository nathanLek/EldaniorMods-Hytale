package com.eldanior.system.titles.nobility.family.definitions.royal;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Eldanior extends NobleFamilyModel {

    public Eldanior() {
        super(
                "eldanior",
                "Eldanior",
                "Par le sang et la couronne, nous regnons.",
                "Fondateurs du royaume, les Eldanior descendent d'Arald Von Eldanior, le capitaine sans terre que le Dragon Primordial "
                + "choisit lors de l'Epreuve du Choix, a la fin de la Guerre Divine. Il fonda le royaume avec les grandes maisons, "
                + "jura le Serment des Deux Rives avec l'Empire Luo et regna mille ans. "
                + "Seul un heritier reconnu par le Dragon au Tombeau des Epreuves Divines peut porter la Couronne d'Aube ; "
                + "depuis la mort du Roi Fou, le trone attend.",
                Rarity.DIVINE,
                NobilityRank.ROI
        );
    }
}