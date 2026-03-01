package com.eldanior.system.skills.skills.passives.Common.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class SixthSense implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.STEALTH_DETECTION) {
            return 1.05f; // +5% de détection des cibles furtives
        }
        return 1.0f;
    }
}