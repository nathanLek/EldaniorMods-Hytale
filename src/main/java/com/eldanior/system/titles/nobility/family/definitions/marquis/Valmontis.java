package com.eldanior.system.titles.nobility.family.definitions.marquis;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Valmontis extends NobleFamilyModel {

    public Valmontis() {
        super(
                "valmontis",
                "Valmontis",
                "L'or est le sang du commerce et du pouvoir.",
                Rarity.LEGENDARY,
                NobilityRank.MARQUIS,
                PassiveSkill.FAMILY_GOLDEN_FORTUNE
        );
    }
}