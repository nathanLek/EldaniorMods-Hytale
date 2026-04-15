package com.eldanior.system.skills.skills.passives.Uncommon.Chance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class CriticalLuck implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.LUCK_CRITICAL) {
            return 5.0f; // +5% critique
        }
        return 0.0f;
    }
}
