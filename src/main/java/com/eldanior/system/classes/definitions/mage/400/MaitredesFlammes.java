package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitredesFlammes extends ClassModel {
    public MaitredesFlammes() {
        super("maitre_des_flammes", "Maitre des Flammes", "Le maitre des flammes dont le feu consume tout sur son passage.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.ARCANE_STRIKE, PassiveSkill.BRILLIANT_MIND, PassiveSkill.KEEN_SENSES), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                7, 7, 44, 7, 10, 10);
    }
}
