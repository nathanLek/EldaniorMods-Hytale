package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GenesesMage extends ClassModel {
    public GenesesMage() {
        super("geneses_mage", "Geneses Mage", "Le mage de la genese qui recree le monde a chaque souffle.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.GENIUS_MIND, PassiveSkill.HEART_OF_GENESIS, PassiveSkill.MANA_OCEAN), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                64, 174, 312, 124, 54, 90);
    }
}
