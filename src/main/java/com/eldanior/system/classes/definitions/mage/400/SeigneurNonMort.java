package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SeigneurNonMort extends ClassModel {
    public SeigneurNonMort() {
        super("seigneur_non_mort", "Seigneur Non-Mort", "Le seigneur des morts-vivants. Son empire s'etend au-dela de la tombe.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.SPIRIT_DRAIN, PassiveSkill.COSMIC_CONSTITUTION, PassiveSkill.ARCANE_SUPREMACY), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                28, 84, 194, 108, 32, 84);
    }
}
