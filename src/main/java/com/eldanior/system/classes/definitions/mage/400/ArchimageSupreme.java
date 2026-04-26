package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArchimageSupreme extends ClassModel {
    public ArchimageSupreme() {
        super("archimage_supreme", "Archimage Supreme", "L'archimage supreme dont le savoir transcende les siecles.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.GENIUS_MIND, PassiveSkill.MANA_OCEAN, PassiveSkill.SPELLBLADE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                16, 48, 108, 32, 36, 36);
    }
}
