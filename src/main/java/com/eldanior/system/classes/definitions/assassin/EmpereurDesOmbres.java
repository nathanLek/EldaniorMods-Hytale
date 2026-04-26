package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class EmpereurDesOmbres extends ClassModel {
    public EmpereurDesOmbres() {
        super("empereur_ombres", "Empereur des Ombres", "L'Empereur des Ombres regne sur un royaume invisible. Chaque ombre dans le monde est son espion et son arme.",
                Rarity.LEGENDARY, ClassType.ASSASSIN,
                List.of(PassiveSkill.SOUL_CRUSHING_PRESSURE, PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.COSMIC_CONSTITUTION),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of("tyran_des_ombres", "monarque_noir", "regne_de_l_ombre"), 400, false,
                100, 80, 30, 60, 220, 200);
    }
}
