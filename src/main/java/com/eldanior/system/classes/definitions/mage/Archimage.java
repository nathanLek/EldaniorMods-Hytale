package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Archimage extends ClassModel {
    public Archimage() {
        super("archimage", "Archimage", "L'Archimage a transcende les limites de la magie ordinaire. Sa connaissance des arcanes est sans egale.",
                Rarity.RARE, ClassType.MAGE,
                List.of(PassiveSkill.EXPANDED_MIND, PassiveSkill.SPELLBLADE, PassiveSkill.MANA_STREAM),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of(), 250, false,
                10, 30, 60, 20, 20, 20);
    }
}
