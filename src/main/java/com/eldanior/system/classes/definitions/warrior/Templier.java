package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Templier extends ClassModel {

    public Templier() {
        super(
                "templier",
                "Templier",
                "Le Templier est un guerrier sacre dont la foi renforce chaque coup. Equilibre parfait entre attaque et defense.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(PassiveSkill.STEEL_RESOLVE, PassiveSkill.MARATHON_RUNNER, PassiveSkill.OVERFLOWING_LIFE),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                250,
                false,
                50, 50, 4, 50, 60, 60
        );
    }
}