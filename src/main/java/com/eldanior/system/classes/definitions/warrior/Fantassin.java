package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Fantassin extends ClassModel {

    public Fantassin() {
        super(
                "fantassin",
                "Fantassin",
                "Le Fantassin est un mur vivant. Sa vitalite et son endurance le rendent presque impossible a abattre.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(PassiveSkill.STONE_SKIN, PassiveSkill.ROBUST_CONSTITUTION, PassiveSkill.IRON_RESOLVE),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of("veteran", "centurion", "legionnaire"),
                400,
                false,
                16, 30, 4, 20, 6, 4
        );
    }
}