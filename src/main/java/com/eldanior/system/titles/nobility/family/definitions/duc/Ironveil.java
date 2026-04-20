package com.eldanior.system.titles.nobility.family.definitions.duc;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Ironveil extends NobleFamilyModel {

    public Ironveil() {
        super(
                "ironveil",
                "Ironveil",
                "Notre volonte est d'acier, notre honneur indestructible.",
                Rarity.EPIC,
                NobilityRank.DUC,
                PassiveSkill.FAMILY_IRON_WILL
        );
    }
}