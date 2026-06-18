package com.eldanior.system.skills.skills.passives.Common.Chance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class FortuneCoins implements IPassiveCombatSkill {

    private static final float LOOT_BONUS = 10.0f;           // +10% coins drop
    private static final float LOOT_BONUS_MASTERED = 11.0f;   // +11% si maîtrisé

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.LUCK_LOOT) {
            return LOOT_BONUS;
        }
        return 0.0f;
    }
    // Progression gérée par DeathXPSystem (chaque kill de mob)
}