package com.eldanior.system.skills.skills.passives.Common.Magique;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class AwakenedMind implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.INTELLIGENCE) {
            return 15.0f; // +15 Intelligence brute (augmente le Mana Max)
        }
        return 0.0f;
    }
}