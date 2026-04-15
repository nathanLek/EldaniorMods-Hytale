package com.eldanior.system.skills.skills.passives.Epique.Chance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class DivineStrike implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.LUCK_CRITICAL) return 12.0f;
        return 0.0f;
    }
}