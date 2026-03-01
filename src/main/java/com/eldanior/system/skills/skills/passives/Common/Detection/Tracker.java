package com.eldanior.system.skills.skills.passives.Common.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class Tracker implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.TRACKING_EVIDENCE) {
            return 1.25f; // +25% de visibilité des traces de mobs
        }
        return 1.0f;
    }
}