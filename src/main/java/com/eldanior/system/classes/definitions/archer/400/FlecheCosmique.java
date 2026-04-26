package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class FlecheCosmique extends ClassModel {
    public FlecheCosmique() {
        super("fleche_cosmique", "Fleche Cosmique", "Ses fleches traversent les dimensions. Chaque tir est cosmique.",
                Rarity.UNIQUE, ClassType.ARCHER, List.of(PassiveSkill.ARCANE_ANNIHILATION, PassiveSkill.STORM_STEP, PassiveSkill.MANA_FORTRESS), List.of(WeaponMastery.BOW, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                54, 48, 82, 36, 126, 96);
    }
}
