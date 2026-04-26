package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitredInvocation extends ClassModel {
    public MaitredInvocation() {
        super("maitre_d_invocation", "Maitre d'Invocation", "Le maitre absolu de l'invocation. Ses creatures sont legendaires.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.AWAKENED_MIND, PassiveSkill.MANAWELL, PassiveSkill.STEEL_CONSTITUTION), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                4, 16, 38, 16, 8, 6);
    }
}
