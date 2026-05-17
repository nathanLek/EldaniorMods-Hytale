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
                "Par la grace divine, nous guerissons les ames et purifions les tenebres.",
                "Les Luminara sont les gardiens de la foi et de la lumiere sacree. "
                + "Leur matriarche fondatrice, Sainte Elaria Luminara, recut une vision divine qui la guida "
                + "vers les Terres du Sud-Ouest, ou elle batit le premier Grand Temple. "
                + "Depuis, la famille produit les plus grands pretres, guerisseurs et paladins du royaume. "
                + "Leur marquisat est un havre de paix ou les pelerins affluent pour recevoir la benediction des Luminara.",
                Rarity.LEGENDARY,
                NobilityRank.MARQUIS,
                PassiveSkill.FAMILY_DIVINE_LIGHT
        );
    }
}