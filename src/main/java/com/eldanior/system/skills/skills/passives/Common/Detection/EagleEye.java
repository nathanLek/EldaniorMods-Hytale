package com.eldanior.system.skills.skills.passives.Common.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class EagleEye implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.DETECTION_RANGE) {
            return 1.15f; // +15% de distance de détection
        }
        return 1.0f;
    }
}