package com.eldanior.system.titles.nobility.family.definitions.marquis;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Ashford extends NobleFamilyModel {

    public Ashford() {
        super(
                "ashford",
                "Ashford",
                "De la cendre, nous renaissons plus forts.",
                Rarity.LEGENDARY,
                NobilityRank.MARQUIS,
                PassiveSkill.FAMILY_PHOENIX_BLOOD
        );
    }
}