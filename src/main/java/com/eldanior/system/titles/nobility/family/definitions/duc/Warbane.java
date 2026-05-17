package com.eldanior.system.titles.nobility.family.definitions.duc;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Warbane extends NobleFamilyModel {

    public Warbane() {
        super(
                "warbane",
                "Warbane",
                "La guerre est notre heritage, la victoire notre destin.",
                "Les Warbane sont nes sur les champs de bataille, vassaux redoutes des Runkandel. "
                + "Leur ancetre, le berserker Grath Warbane, mit fin a la Guerre des Cent Lames "
                + "en defiant et vainquant douze champions ennemis en combat singulier. "
                + "Les guerriers Warbane sont formes des l'enfance a l'art brutal du combat rapproche. "
                + "On dit que leur cri de guerre glace le sang meme des veterans les plus aguerris.",
                Rarity.EPIC,
                NobilityRank.DUC,
                PassiveSkill.FAMILY_IRON_WILL
        );
    }
}
