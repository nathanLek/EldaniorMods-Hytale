package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArchonteSupreme extends ClassModel {
    public ArchonteSupreme() {
        super("archonte_supreme", "Archonte Supreme", "L'archonte supreme dont la volonte facon le monde.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.GENIUS_MIND, PassiveSkill.ARCANE_ANNIHILATION, PassiveSkill.MANA_INFINITY), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                52, 136, 340, 136, 68, 102);
    }
}
