package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PropheteAbsolu extends ClassModel {
    public PropheteAbsolu() {
        super("prophete_absolu", "Prophete Absolu", "Le prophete absolu dont chaque parole est verite.",
                Rarity.LEGENDARY, ClassType.MAGE, List.of(PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.COSMIC_MIND, PassiveSkill.FATE_DODGE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                68, 170, 476, 136, 136, 204);
    }
}
