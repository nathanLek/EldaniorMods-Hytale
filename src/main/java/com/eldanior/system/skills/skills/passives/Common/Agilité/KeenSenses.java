package com.eldanior.system.skills.skills.passives.Common.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class KeenSenses implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.LUCK_CRITICAL) {
            return 1.02f; // +2% de chance de coup critique
        }
        return 1.0f;
    }
}