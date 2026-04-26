package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GrandArchimage extends ClassModel {
    public GrandArchimage() {
        super("grand_archimage", "Grand Archimage", "Le plus grand archimage vivant. Sa puissance magique est legendaire.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.BRILLIANT_MIND, PassiveSkill.MANA_FORTRESS, PassiveSkill.ARCANE_DEVASTATION), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                18, 52, 104, 34, 34, 34);
    }
}
