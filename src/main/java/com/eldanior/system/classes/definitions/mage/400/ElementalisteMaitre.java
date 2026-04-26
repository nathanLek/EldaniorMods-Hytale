package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ElementalisteMaitre extends ClassModel {
    public ElementalisteMaitre() {
        super("elementaliste_maitre", "Elementaliste Maitre", "Le maitre des quatre elements. Sa puissance elementaire est sans egale.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.AWAKENED_MIND, PassiveSkill.ARCANE_STRIKE, PassiveSkill.MANA_STREAM), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                4, 10, 44, 10, 10, 7);
    }
}
