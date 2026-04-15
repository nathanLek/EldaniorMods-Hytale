package com.eldanior.system.skills.skills.passives.Rare.Regeneration;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class ManaRiver implements IPassiveCombatSkill {
    @Override
    public float getRegenMultiplier(StatConfig stat) {
        if (stat == StatConfig.INTELLIGENCE) return 3.0f;
        return 1.0f;
    }
}