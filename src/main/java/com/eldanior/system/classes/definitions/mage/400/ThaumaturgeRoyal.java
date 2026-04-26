package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ThaumaturgeRoyal extends ClassModel {
    public ThaumaturgeRoyal() {
        super("thaumaturge_royal", "Thaumaturge Royal", "Le thaumaturge du roi dont les prodiges protegent le royaume.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.UNLEASHED_MAGIC, PassiveSkill.BURSTING_LIFE, PassiveSkill.MANA_FORTRESS), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                16, 62, 82, 46, 24, 40);
    }
}
