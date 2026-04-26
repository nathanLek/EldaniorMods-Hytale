package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PhilosopheMage extends ClassModel {
    public PhilosopheMage() {
        super("philosophe_mage", "Philosophe Mage", "Un philosophe-mage qui cherche la verite ultime par l'alchimie.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.EXPANDED_MIND, PassiveSkill.TROLL_BLOOD, PassiveSkill.BRILLIANT_MIND), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                8, 42, 76, 32, 24, 82);
    }
}
