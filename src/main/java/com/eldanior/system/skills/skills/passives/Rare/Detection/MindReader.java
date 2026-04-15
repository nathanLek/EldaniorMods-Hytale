package com.eldanior.system.skills.skills.passives.Rare.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class MindReader implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.STEALTH_DETECTION) return 1.15f;
        return 1.0f;
    }
}