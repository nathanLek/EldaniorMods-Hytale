package com.eldanior.system.skills.skills.passives.Common.Chance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class LuckyStrike implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.LUCK_CRITICAL) {
            return 3.0f; // +3% de chance de critique
        }
        return 0.0f;
    }
}