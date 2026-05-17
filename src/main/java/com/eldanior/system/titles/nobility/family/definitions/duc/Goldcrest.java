package com.eldanior.system.titles.nobility.family.definitions.duc;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Goldcrest extends NobleFamilyModel {

    public Goldcrest() {
        super(
                "goldcrest",
                "Goldcrest",
                "L'or ouvre toutes les portes que l'epee ne peut briser.",
                "Les Goldcrest sont les banquiers du royaume, la branche financiere du Marquisat Valmontis. "
                + "Leur fondateur, Aurelius Goldcrest, inventa le systeme de lettres de credit "
                + "qui revolutionna le commerce entre les territoires. Leur duche abrite la Grande Banque, "
                + "ou sont conservees les reserves d'or de la noblesse. "
                + "On dit que les Goldcrest peuvent acheter une armee sans degainer une seule epee.",
                Rarity.EPIC,
                NobilityRank.DUC,
                PassiveSkill.FAMILY_GOLDEN_FORTUNE
        );
    }
}
