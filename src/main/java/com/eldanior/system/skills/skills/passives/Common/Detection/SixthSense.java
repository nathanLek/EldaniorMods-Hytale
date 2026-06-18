package com.eldanior.system.skills.skills.passives.Common.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class SixthSense implements IPassiveCombatSkill {

    private static final float STEALTH_BONUS = 1.05f;
    private static final float STEALTH_BONUS_MASTERED = 1.055f;

    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.STEALTH_DETECTION) {
            return STEALTH_BONUS;
        }
        return 1.0f;
    }
    // Détection des invisibles gérée par DetectionSystem
    // Progression par message radar + bonus quand un invisible est détecté
}