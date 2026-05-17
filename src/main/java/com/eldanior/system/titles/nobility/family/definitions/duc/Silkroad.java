package com.eldanior.system.titles.nobility.family.definitions.duc;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Silkroad extends NobleFamilyModel {

    public Silkroad() {
        super(
                "silkroad",
                "Silkroad",
                "Les routes du commerce sont les veines du royaume.",
                "Les Silkroad sont les maitres caravaniers et negociants du Marquisat Valmontis. "
                + "Leur fondatrice, Isara Silkroad, ouvrit la premiere route commerciale vers les terres lointaines, "
                + "rapportant des epices, des tissus et des savoirs inconnus. "
                + "Leur duche est un carrefour de cultures ou les marchands du monde entier se retrouvent. "
                + "Les Silkroad connaissent le prix de chaque chose et la valeur de chaque alliance.",
                Rarity.EPIC,
                NobilityRank.DUC,
                PassiveSkill.FAMILY_GOLDEN_FORTUNE
        );
    }
}
