package com.eldanior.system.skills.skills.passives.Unique.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class TrueSight implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.STEALTH_DETECTION) return 1.25f;
        return 1.0f;
    }
}