package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Paladin extends ClassModel {

    public Paladin() {
        super(
                "paladin",
                "Paladin",
                "Le Paladin est un guerrier sacre dont la foi inébranlable renforce chaque coup. Il protege les faibles et punit les impurs.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(PassiveSkill.STEEL_RESOLVE, PassiveSkill.OVERFLOWING_LIFE, PassiveSkill.VITAL_RECOVERY),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD, WeaponMastery.MACE),
                List.of("paladin_sacre", "croise_divin", "justicier"),
                400,
                false,
                40, 50, 20, 40, 20, 30
        );
    }
}