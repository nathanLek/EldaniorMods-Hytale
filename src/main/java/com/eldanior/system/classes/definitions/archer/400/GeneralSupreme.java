package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GeneralSupreme extends ClassModel {
    public GeneralSupreme() {
        super("general_supreme", "General Supreme", "Le general supreme des armees, maitre de la guerre a distance.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.COSMIC_CONSTITUTION, PassiveSkill.FATAL_PRECISION, PassiveSkill.LIGHTNING_REFLEXES), List.of(WeaponMastery.BOW, WeaponMastery.SWORD), List.of(), 400, false,
                82, 100, 36, 82, 100, 108);
    }
}
