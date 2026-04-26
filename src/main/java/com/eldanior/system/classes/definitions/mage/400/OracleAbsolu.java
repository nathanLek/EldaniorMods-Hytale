package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OracleAbsolu extends ClassModel {
    public OracleAbsolu() {
        super("oracle_absolu", "Oracle Absolu", "L'oracle dont les visions sont toujours exactes. Il voit tout.",
                Rarity.UNIQUE, ClassType.MAGE, List.of(PassiveSkill.DESTINY_STRIKE, PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.PURE_MAGIC), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                18, 70, 174, 52, 70, 140);
    }
}
