package com.eldanior.system.skills.skills.passives.Common.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class EagleEye implements IPassiveCombatSkill {

    private static final float RANGE_BONUS = 1.15f;
    private static final float RANGE_BONUS_MASTERED = 1.165f;

    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.DETECTION_RANGE) {
            return RANGE_BONUS;
        }
        return 1.0f;
    }
    // Progression gérée par DetectionSystem (chaque message radar)
}
