package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MagusSupreme extends ClassModel {
    public MagusSupreme() {
        super("magus_supreme", "Magus Supreme", "Le magus supreme dont la puissance magique eclipserait le soleil.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.GENIUS_MIND, PassiveSkill.ARCANE_ANNIHILATION, PassiveSkill.MANA_INFINITY), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                34, 104, 208, 86, 52, 70);
    }
}
