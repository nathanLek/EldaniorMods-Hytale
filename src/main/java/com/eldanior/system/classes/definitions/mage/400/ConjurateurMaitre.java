package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ConjurateurMaitre extends ClassModel {
    public ConjurateurMaitre() {
        super("conjurateur_maitre", "Conjurateur Maitre", "Un maitre conjurateur qui fait apparaitre des forces primordiales.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.MANA_FONT, PassiveSkill.BRILLIANT_MIND, PassiveSkill.ROBUST_CONSTITUTION), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                2, 12, 42, 12, 6, 8);
    }
}
