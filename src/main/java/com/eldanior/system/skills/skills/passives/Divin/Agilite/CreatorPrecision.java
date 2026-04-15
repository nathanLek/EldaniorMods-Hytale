package com.eldanior.system.skills.skills.passives.Divin.Agilite;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class CreatorPrecision implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.LUCK_CRITICAL) return 1.18f;
        return 1.0f;
    }
}