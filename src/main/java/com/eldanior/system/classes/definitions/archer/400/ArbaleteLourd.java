package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArbaleteLourd extends ClassModel {
    public ArbaleteLourd() {
        super("arbalete_lourd", "Arbalete Lourd", "La puissance brute de l'arbalete lourde. Chaque tir est devastateur.",
                Rarity.RARE, ClassType.ARCHER, List.of(PassiveSkill.INSTINCTIVE_STRIKE, PassiveSkill.SHARP_BLADE, PassiveSkill.STONE_SKIN), List.of(WeaponMastery.BOW), List.of(), 400, false,
                16, 12, 2, 12, 18, 18);
    }
}
