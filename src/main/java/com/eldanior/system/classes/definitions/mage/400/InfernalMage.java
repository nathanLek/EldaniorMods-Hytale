package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class InfernalMage extends ClassModel {
    public InfernalMage() {
        super("infernal_mage", "Infernal Mage", "Un mage infernal dont les flammes brulent meme les ames.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.ARCANE_STRIKE, PassiveSkill.SPELLBLADE, PassiveSkill.INSTINCTIVE_STRIKE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                8, 6, 42, 6, 12, 8);
    }
}
