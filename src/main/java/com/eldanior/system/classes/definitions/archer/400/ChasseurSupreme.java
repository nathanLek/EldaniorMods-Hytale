package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ChasseurSupreme extends ClassModel {
    public ChasseurSupreme() {
        super("chasseur_supreme", "Chasseur Supreme", "Le chasseur ultime. Sa traque est une science perfectionnee.",
                Rarity.EPIC, ClassType.ARCHER, List.of(PassiveSkill.RELENTLESS_HUNT, PassiveSkill.FATAL_PRECISION, PassiveSkill.RAZOR_SENSES), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                34, 34, 8, 26, 96, 78);
    }
}
