package com.eldanior.system.skills.skills.passives.Epique.Regeneration;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class ManaOcean implements IPassiveCombatSkill {
    @Override
    public float getRegenMultiplier(StatConfig stat) {
        if (stat == StatConfig.INTELLIGENCE) return 4.0f;
        return 1.0f;
    }
}