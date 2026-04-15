package com.eldanior.system.skills.skills.passives.Uncommon.Chance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class GoldenTouch implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.LUCK_LOOT) {
            return 20.0f; // +20% coins des mobs
        }
        return 0.0f;
    }
}
