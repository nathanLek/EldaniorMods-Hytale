package com.eldanior.system.titles.nobility.family.definitions.marquis;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Luminara extends NobleFamilyModel {

    public Luminara() {
        super(
                "luminara",
                "Luminara",
                "La lumiere guide nos pas et nos lames.",
                Rarity.LEGENDARY,
                NobilityRank.MARQUIS,
                PassiveSkill.FAMILY_DIVINE_LIGHT
        );
    }
}