package com.eldanior.system.skills.skills.passives.Uncommon.Defense;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class IronBody implements IPassiveCombatSkill {

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.ENDURANCE_DEFENSE) {
            return 25.0f; // +25 Endurance Défense
        }
        return 0.0f;
    }
}