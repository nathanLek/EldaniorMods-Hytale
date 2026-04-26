package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Mystique extends ClassModel {
    public Mystique() {
        super("mystique", "Mystique", "Le Mystique explore les mysteres de l'univers. Sa comprehension des forces cosmiques lui confere un pouvoir unique.",
                Rarity.RARE, ClassType.MAGE,
                List.of(PassiveSkill.EXPANDED_MIND, PassiveSkill.ASTRAL_CLOAK, PassiveSkill.CRITICAL_LUCK),
                List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK),
                List.of("mystique_superieur", "voyant_arcane", "devin_cosmique"), 400, false,
                6, 25, 55, 20, 20, 35);
    }
}
