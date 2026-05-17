package com.eldanior.system.titles.nobility.family.definitions.duc;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Nighthollow extends NobleFamilyModel {

    public Nighthollow() {
        super(
                "nighthollow",
                "Nighthollow",
                "Les ombres sont notre arme, le silence notre bouclier.",
                "Les Nighthollow operent depuis les terres du Marquisat Luminara, un paradoxe que beaucoup ne comprennent pas. "
                + "Pourtant, leur fondatrice, Dame Nyx Nighthollow, etait une ancienne pretresse Luminara "
                + "qui comprit que la lumiere ne peut exister sans l'ombre. Elle crea l'Ordre du Crepuscule, "
                + "un reseau d'espions et d'assassins au service exclusif de la couronne. "
                + "Les Nighthollow eliminent les menaces avant qu'elles ne naissent, dans le silence absolu.",
                Rarity.EPIC,
                NobilityRank.DUC,
                PassiveSkill.FAMILY_SHADOW_STRIKE
        );
    }
}
