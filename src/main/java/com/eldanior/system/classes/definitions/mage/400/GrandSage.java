package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GrandSage extends ClassModel {
    public GrandSage() {
        super("grand_sage", "Grand Sage", "Le plus grand sage vivant. Sa sagesse eclaire le monde.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.STEEL_CONSTITUTION, PassiveSkill.BRILLIANT_MIND, PassiveSkill.MANA_OCEAN), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                34, 52, 86, 52, 34, 34);
    }
}
