package com.eldanior.system.skills.skills.passives.Epique.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class OmniscientVision implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.DETECTION_RANGE) return 1.50f;
        return 1.0f;
    }
}