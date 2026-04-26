package com.eldanior.system.classes.definitions.mage;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class OracleDivin extends ClassModel {
    public OracleDivin() {
        super("oracle_divin", "Oracle Divin", "L'oracle divin qui voit la fin et le commencement de tout.",
                Rarity.LEGENDARY, ClassType.MAGE, List.of(PassiveSkill.CREATOR_PRECISION, PassiveSkill.INFINITE_MIND, PassiveSkill.GENESIS_STRIKE), List.of(WeaponMastery.STAFF, WeaponMastery.SPELLBOOK), List.of(), 400, false,
                72, 166, 472, 132, 140, 200);
    }
}
