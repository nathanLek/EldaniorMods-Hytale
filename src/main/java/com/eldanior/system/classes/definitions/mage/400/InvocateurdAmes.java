package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class InvocateurdAmes extends ClassModel {
    public InvocateurdAmes() {
        super("invocateur_d_ames", "Invocateur d'Ames", "Il invoque les ames des defunts pour combattre a ses cotes.",
                Rarity.RARE, ClassType.MAGE, List.of(PassiveSkill.SPIRIT_DRAIN, PassiveSkill.BRILLIANT_MIND, PassiveSkill.LUCKY_STRIKE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                8, 8, 42, 6, 8, 10);
    }
}
