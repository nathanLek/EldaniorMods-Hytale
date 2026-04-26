package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DevinCosmique extends ClassModel {
    public DevinCosmique() {
        super("devin_cosmique", "Devin Cosmique", "Un devin connecte aux forces cosmiques de l'univers.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.GENIUS_MIND, PassiveSkill.RAZOR_SENSES, PassiveSkill.ASTRAL_CLOAK), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                8, 42, 100, 36, 32, 58);
    }
}
