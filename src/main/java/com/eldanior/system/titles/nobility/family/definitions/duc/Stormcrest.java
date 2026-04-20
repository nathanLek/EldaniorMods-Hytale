package com.eldanior.system.titles.nobility.family.definitions.duc;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Stormcrest extends NobleFamilyModel {

    public Stormcrest() {
        super(
                "stormcrest",
                "Stormcrest",
                "Nous sommes la tempete que nul ne peut arreter.",
                Rarity.EPIC,
                NobilityRank.DUC,
                PassiveSkill.FAMILY_STORM_VIGOR
        );
    }
}