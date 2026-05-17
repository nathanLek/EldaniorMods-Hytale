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
                "Les Valmontis sont la plus puissante dynastie marchande du royaume. "
                + "Leur empire commercial s'etend bien au-dela des frontieres, controlant les routes de la soie, "
                + "les mines precieuses et les grands marches. Le patriarche fondateur, Mercurio Valmontis, "
                + "transforma un simple comptoir en un reseau commercial tentaculaire. "
                + "Leur marquisat au sud-est regorge de bazars, d'entrepots et de banques ou circulent les fortunes du monde.",
                Rarity.LEGENDARY,
                NobilityRank.MARQUIS,
                PassiveSkill.FAMILY_GOLDEN_FORTUNE
        );
    }
}