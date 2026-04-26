package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TransmutateurDivin extends ClassModel {
    public TransmutateurDivin() {
        super("transmutateur_divin", "Transmutateur Divin", "Le transmutateur dont les creations rivalisent avec celles des dieux.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.GOLDEN_TOUCH, PassiveSkill.MANA_OCEAN, PassiveSkill.ENRICHED_BLOOD), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                12, 46, 74, 36, 28, 84);
    }
}
