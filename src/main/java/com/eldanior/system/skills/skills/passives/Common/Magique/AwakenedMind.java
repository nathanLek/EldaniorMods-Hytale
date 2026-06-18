package com.eldanior.system.skills.skills.passives.Common.Magique;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class AwakenedMind implements IPassiveCombatSkill {

    private static final float INTEL_BONUS = 15.0f;
    private static final float INTEL_BONUS_MASTERED = 16.5f;

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.INTELLIGENCE) {
            return INTEL_BONUS;
        }
        return 0.0f;
    }
    // Progression gérée par MovementTrackingSystem (synchro avec points intelligence)
}
