package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ElementaireSupreme extends ClassModel {
    public ElementaireSupreme() {
        super("elementaire_supreme", "Elementaire Supreme", "L'elementaire supreme dont la maitrise est absolue.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.ARCANE_DEVASTATION, PassiveSkill.COSMIC_MIND, PassiveSkill.LIGHTNING_REFLEXES), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                40, 82, 204, 66, 90, 50);
    }
}
