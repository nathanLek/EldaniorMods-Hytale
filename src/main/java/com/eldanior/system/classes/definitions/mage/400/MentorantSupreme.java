package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MentorantSupreme extends ClassModel {
    public MentorantSupreme() {
        super("mentorant_supreme", "Mentorant Supreme", "Le mentor supreme qui forme les plus grands mages du monde.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.STEEL_CONSTITUTION, PassiveSkill.EXPANDED_MIND, PassiveSkill.OVERFLOWING_LIFE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                36, 54, 84, 54, 32, 32);
    }
}
