package com.eldanior.system.titles.nobility.family.definitions.duc;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Frostguard extends NobleFamilyModel {

    public Frostguard() {
        super(
                "frostguard",
                "Frostguard",
                "L'hiver ne nous brise pas, il nous forge.",
                Rarity.EPIC,
                NobilityRank.DUC,
                PassiveSkill.FAMILY_FROST_RESILIENCE
        );
    }
}