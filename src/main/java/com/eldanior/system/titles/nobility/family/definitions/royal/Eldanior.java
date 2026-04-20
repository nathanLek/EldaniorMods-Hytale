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
                Rarity.DIVINE,
                NobilityRank.ROI,
                PassiveSkill.FAMILY_ROYAL_AUTHORITY
        );
    }
}