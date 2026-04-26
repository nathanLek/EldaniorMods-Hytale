package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MageNoir extends ClassModel {
    public MageNoir() {
        super("mage_noir", "Mage Noir", "Un mage corrompu par les arts sombres. Sa puissance est terrifiante.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.SPIRIT_DRAIN, PassiveSkill.RAZOR_SENSES, PassiveSkill.SPELLBLADE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                16, 32, 92, 24, 36, 66);
    }
}
