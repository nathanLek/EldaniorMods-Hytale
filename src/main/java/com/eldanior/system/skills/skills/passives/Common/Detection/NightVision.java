package com.eldanior.system.skills.skills.passives.Common.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class NightVision implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.LOW_LIGHT_VISIBILITY) {
            return 1.20f; // +20% de visibilité en basse lumière
        }
        return 1.0f;
    }
}