package com.eldanior.system.skills.skills.passives.Uncommon.Magique;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class ExpandedMind implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.INTELLIGENCE) {
            return 25.0f; // +25 Intelligence
        }
        return 0.0f;
    }
}