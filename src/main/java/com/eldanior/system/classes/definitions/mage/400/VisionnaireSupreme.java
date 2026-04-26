package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class VisionnaireSupreme extends ClassModel {
    public VisionnaireSupreme() {
        super("visionnaire_supreme", "Visionnaire Supreme", "Le visionnaire supreme dont le regard perce les voiles du temps.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.EAGLE_VISION, PassiveSkill.FATAL_PRECISION, PassiveSkill.COSMIC_MIND), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                16, 66, 170, 50, 72, 138);
    }
}
