package com.eldanior.system.skills.skills.passives.Common.Chance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class TreasureHunter implements IPassiveCombatSkill {

    private static final float LOOT_BONUS = 5.0f;
    private static final float LOOT_BONUS_MASTERED = 5.5f;

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.LUCK_LOOT) {
            return LOOT_BONUS;
        }
        return 0.0f;
    }
    // Progression gérée par TreasureChestRangeSystem (découverte de coffre)
}
