package com.eldanior.system.titles.nobility.family.definitions.royal;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Eldanior extends NobleFamilyModel {

    public Eldanior() {
        super(
                "eldanior",
                "Eldanior",
                "Par le sang et la couronne, nous regnons.",
                "Fondateurs du royaume, les Eldanior descendent du premier roi qui unifia les terres sous une seule banniere. "
                + "Leur lignee remonte a l'Age des Fondations, quand Aelric Eldanior forgea le pacte sacre avec les quatre grandes maisons. "
                + "Depuis, chaque souverain porte la Couronne d'Aube, symbole de l'alliance eternelle entre le trone et le peuple. "
                + "On dit que le sang royal confere une autorite naturelle que meme les plus feroces guerriers respectent.",
                Rarity.DIVINE,
                NobilityRank.ROI,
                PassiveSkill.FAMILY_ROYAL_AUTHORITY
        );
    }
}