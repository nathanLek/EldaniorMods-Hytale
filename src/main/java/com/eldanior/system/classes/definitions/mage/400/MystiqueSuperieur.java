package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MystiqueSuperieur extends ClassModel {
    public MystiqueSuperieur() {
        super("mystique_superieur", "Mystique Superieur", "Un mystique dont les visions percent les voiles de la realite.",
                Rarity.EPIC, ClassType.MAGE, List.of(PassiveSkill.EXPANDED_MIND, PassiveSkill.CRITICAL_LUCK, PassiveSkill.ASTRAL_CLOAK), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                10, 44, 96, 34, 34, 60);
    }
}
