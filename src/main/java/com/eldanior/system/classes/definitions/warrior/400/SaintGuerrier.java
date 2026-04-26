package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SaintGuerrier extends ClassModel {
    public SaintGuerrier() {
        super("saint_guerrier", "Saint Guerrier", "Le Saint Guerrier est beni par les forces celestes. Sa purete d'ame et sa force divine guerissent ses allies et aneantissent les forces du mal.",
                Rarity.UNIQUE, ClassType.WARRIOR, List.of(PassiveSkill.STEEL_BODY, PassiveSkill.BURSTING_LIFE, PassiveSkill.HEART_OF_ETERNITY), List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD, WeaponMastery.MACE), List.of(), 400, false,
                132, 168, 70, 166, 66, 66);
    }
}