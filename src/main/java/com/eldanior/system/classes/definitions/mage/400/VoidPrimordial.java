package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class VoidPrimordial extends ClassModel {
    public VoidPrimordial() {
        super("void_primordial", "Void Primordial", "Le vide primordial d'ou toute magie est nee.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.ARCANE_SUPREMACY, PassiveSkill.DIMENSIONAL_DODGE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                36, 98, 300, 82, 100, 140);
    }
}
