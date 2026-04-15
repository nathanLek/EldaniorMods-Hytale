package com.eldanior.system.skills.skills.passives.Unique.Regeneration;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class ManaInfinity implements IPassiveCombatSkill {
    @Override
    public float getRegenMultiplier(StatConfig stat) {
        if (stat == StatConfig.INTELLIGENCE) return 6.0f;
        return 1.0f;
    }
}