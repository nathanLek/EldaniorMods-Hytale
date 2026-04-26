package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class DemiMage extends ClassModel {
    public DemiMage() {
        super("demi_mage", "Demi-Mage", "Mi-dieu mi-mage. Sa magie est celle qui a cree l'univers.",
                Rarity.DIVINE, ClassType.MAGE, List.of(PassiveSkill.CREATOR_MIND, PassiveSkill.ARCANE_CREATION, PassiveSkill.CREATOR_CONSTITUTION), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                170, 425, 935, 340, 204, 425);
    }
}
