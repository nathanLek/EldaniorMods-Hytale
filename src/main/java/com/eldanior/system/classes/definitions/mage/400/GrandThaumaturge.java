package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GrandThaumaturge extends ClassModel {
    public GrandThaumaturge() {
        super("grand_thaumaturge", "Grand Thaumaturge", "Le plus grand thaumaturge. Ses miracles sont des realites.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.UNLEASHED_MAGIC, PassiveSkill.TITAN_CONSTITUTION, PassiveSkill.MANA_OCEAN), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                14, 60, 86, 44, 26, 44);
    }
}
