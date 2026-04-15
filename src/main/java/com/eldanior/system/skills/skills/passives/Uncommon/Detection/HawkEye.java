package com.eldanior.system.skills.skills.passives.Uncommon.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class HawkEye implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.DETECTION_RANGE) {
            return 1.25f; // +25% distance de détection
        }
        return 1.0f;
    }
}