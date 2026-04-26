package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MagusEternel extends ClassModel {
    public MagusEternel() {
        super("magus_eternel", "Magus Eternel", "Un magus eternel qui transcende le temps par sa puissance.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.COSMIC_MIND, PassiveSkill.ARCANE_SUPREMACY, PassiveSkill.MANA_CITADEL), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                36, 98, 212, 88, 54, 66);
    }
}
