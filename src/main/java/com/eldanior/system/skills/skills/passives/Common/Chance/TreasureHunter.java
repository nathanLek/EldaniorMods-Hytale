package com.eldanior.system.skills.skills.passives.Common.Chance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class TreasureHunter implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.LUCK_LOOT) {
            return 5.0f; // +5% de chance de loot
        }
        return 0.0f;
    }
}