package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TirFatal extends ClassModel {
    public TirFatal() {
        super("tir_fatal", "Tir Fatal", "Un seul tir, une seule fleche. C'est tout ce qu'il faut.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.FATAL_PRECISION, PassiveSkill.ABYSS_BLADE, PassiveSkill.BERSERKER_SWIFTNESS), List.of(WeaponMastery.BOW), List.of(), 400, false,
                64, 54, 28, 36, 140, 168);
    }
}
