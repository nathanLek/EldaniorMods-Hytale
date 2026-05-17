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
                "Branche cadette des Zippel, les Frostguard se sont specialises dans la magie de glace et de protection. "
                + "Leur fondateur, Kaelen Frostguard, maitrisa les tempetes de givre qui devastaient les terres du nord "
                + "et en fit une arme redoutable. Leurs mages-boucliers sont les meilleurs defenseurs magiques du royaume, "
                + "capables d'eriger des remparts de glace impenetrables en un instant.",
                Rarity.EPIC,
                NobilityRank.DUC,
                PassiveSkill.FAMILY_FROST_RESILIENCE
        );
    }
}
