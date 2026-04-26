package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class BrasierVivant extends ClassModel {
    public BrasierVivant() {
        super("brasier_vivant", "Brasier Vivant", "Un brasier vivant qui consume ses ennemis par sa seule presence.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.ARCANE_STRIKE, PassiveSkill.EXPANDED_MIND, PassiveSkill.RAZOR_SENSES), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                6, 8, 46, 8, 8, 12);
    }
}
