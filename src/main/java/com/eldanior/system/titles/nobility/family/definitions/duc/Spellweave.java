package com.eldanior.system.titles.nobility.family.definitions.duc;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Spellweave extends NobleFamilyModel {

    public Spellweave() {
        super(
                "spellweave",
                "Spellweave",
                "L'arcane coule en nous comme le sang dans nos veines.",
                "Les Spellweave sont des tisserands d'arcane, heritiers d'une tradition magique millenaire au sein du Marquisat Zippel. "
                + "Leur fondatrice, Seraphina Spellweave, decouvrit l'art de tisser plusieurs sorts simultanement, "
                + "une technique revolutionnaire qui changea l'art de la guerre magique. "
                + "Leurs enchanteurs sont capables de creer des artefacts d'une puissance inegalee.",
                Rarity.EPIC,
                NobilityRank.DUC,
                PassiveSkill.FAMILY_DIVINE_LIGHT
        );
    }
}
