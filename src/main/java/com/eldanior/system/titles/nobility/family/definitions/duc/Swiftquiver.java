package com.eldanior.system.titles.nobility.family.definitions.duc;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Swiftquiver extends NobleFamilyModel {

    public Swiftquiver() {
        super(
                "swiftquiver",
                "Swiftquiver",
                "Chaque fleche trouve sa cible, chaque cible trouve sa fin.",
                "Les Swiftquiver sont les maitres archers du royaume, vassaux des Luminara dans les forets du sud-ouest. "
                + "Leur fondateur, Finrod Swiftquiver, etait un chasseur legendaire capable d'abattre un aigle en plein vol "
                + "a trois cents pas. Les archers Swiftquiver gardent les frontieres boisees du marquisat "
                + "et leur precision est si redoutee que les armees ennemies refusent de traverser leurs forets. "
                + "On dit qu'aucune fleche Swiftquiver n'a jamais manque sa cible.",
                Rarity.EPIC,
                NobilityRank.DUC,
                PassiveSkill.FAMILY_SHADOW_STRIKE
        );
    }
}
