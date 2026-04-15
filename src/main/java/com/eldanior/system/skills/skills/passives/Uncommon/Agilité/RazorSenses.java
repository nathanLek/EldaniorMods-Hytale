package com.eldanior.system.skills.skills.passives.Uncommon.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class RazorSenses implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.LUCK_CRITICAL) {
            return 1.04f; // +4% chance critique
        }
        return 1.0f;
    }
}