package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DieuDesArcanes extends ClassModel {
    public DieuDesArcanes() {
        super("dieu_des_arcanes", "Dieu des Arcanes", "Le Dieu des Arcanes transcende la mortalite. Sa maitrise absolue de la magie le place au sommet de l'existence.",
                Rarity.DIVINE, ClassType.MAGE,
                List.of(PassiveSkill.CREATOR_MIND, PassiveSkill.ARCANE_CREATION, PassiveSkill.CREATOR_BLOOD),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("demi_mage"), 400, false,
                100, 250, 550, 200, 120, 250);
    }
}
