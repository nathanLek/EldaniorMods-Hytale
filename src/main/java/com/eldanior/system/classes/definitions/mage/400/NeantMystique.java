package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class NeantMystique extends ClassModel {
    public NeantMystique() {
        super("neant_mystique", "Neant Mystique", "Le neant mystique qui precede toute creation.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.VOID_BLADE, PassiveSkill.COSMIC_MIND, PassiveSkill.VOID_STEP), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                32, 106, 310, 90, 98, 132);
    }
}
