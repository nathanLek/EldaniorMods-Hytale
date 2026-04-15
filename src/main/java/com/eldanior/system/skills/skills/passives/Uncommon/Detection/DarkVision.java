package com.eldanior.system.skills.skills.passives.Uncommon.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class DarkVision implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.LOW_LIGHT_VISIBILITY) {
            return 1.40f; // +40% visibilité basse lumière
        }
        return 1.0f;
    }
}