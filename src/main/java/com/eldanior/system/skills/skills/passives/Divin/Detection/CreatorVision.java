package com.eldanior.system.skills.skills.passives.Divin.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class CreatorVision implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.DETECTION_RANGE) return 2.20f;
        return 1.0f;
    }
}