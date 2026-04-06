package com.eldanior.system.skills.skills.passives.Divin.Regeneration;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class ManaHeart implements IPassiveCombatSkill {

    @Override
    public float getRegenMultiplier(StatConfig stat) {
        if (stat == StatConfig.INTELLIGENCE) {
            return 100.5f;
        }
        return 1.0f;
    }
}