package com.eldanior.system.titles.nobility.family.definitions.marquis;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Drakenhart extends NobleFamilyModel {

    public Drakenhart() {
        super(
                "drakenhart",
                "Drakenhart",
                "Le feu du dragon coule dans nos veines.",
                Rarity.LEGENDARY,
                NobilityRank.MARQUIS,
                PassiveSkill.FAMILY_DRAGON_FURY
        );
    }
}