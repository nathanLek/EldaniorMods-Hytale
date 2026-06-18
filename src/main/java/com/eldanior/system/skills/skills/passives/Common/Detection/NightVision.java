package com.eldanior.system.skills.skills.passives.Common.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class NightVision implements IPassiveCombatSkill {

    private static final float VISIBILITY_BONUS = 1.20f;
    private static final float VISIBILITY_BONUS_MASTERED = 1.22f;

    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.LOW_LIGHT_VISIBILITY) {
            return VISIBILITY_BONUS;
        }
        return 1.0f;
    }
    // Progression gérée par MovementTrackingSystem (toutes les 5min la nuit)
    // + DetectionSystem (chaque message radar)
}
