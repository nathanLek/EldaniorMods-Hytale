package com.eldanior.system.skills.skills.passives.Unique.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class AbsolutePrecision implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.LUCK_CRITICAL) return 1.12f;
        return 1.0f;
    }
}