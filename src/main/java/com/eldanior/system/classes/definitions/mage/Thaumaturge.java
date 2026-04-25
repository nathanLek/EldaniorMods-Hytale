package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Thaumaturge extends ClassModel {
    public Thaumaturge() {
        super("thaumaturge", "Thaumaturge", "Le Thaumaturge accomplit des miracles par la force de sa volonte. Sa magie transcende les lois naturelles.",
                Rarity.RARE, ClassType.MAGE,
                List.of(PassiveSkill.UNLEASHED_MAGIC, PassiveSkill.OVERFLOWING_LIFE, PassiveSkill.MANA_STREAM),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of(), 250, false,
                8, 35, 50, 25, 15, 25);
    }
}
