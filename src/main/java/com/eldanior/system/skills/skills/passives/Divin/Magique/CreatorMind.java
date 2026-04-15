package com.eldanior.system.skills.skills.passives.Divin.Magique;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class CreatorMind implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.INTELLIGENCE) return 120.0f;
        return 0.0f;
    }
}