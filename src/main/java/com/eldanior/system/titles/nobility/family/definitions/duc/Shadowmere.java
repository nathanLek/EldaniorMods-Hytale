package com.eldanior.system.titles.nobility.family.definitions.duc;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Shadowmere extends NobleFamilyModel {

    public Shadowmere() {
        super(
                "shadowmere",
                "Shadowmere",
                "Dans l'ombre, nous frappons sans etre vus.",
                Rarity.EPIC,
                NobilityRank.DUC,
                PassiveSkill.FAMILY_SHADOW_STRIKE
        );
    }
}