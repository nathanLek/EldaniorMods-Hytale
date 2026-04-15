package com.eldanior.system.skills.skills.passives.Uncommon.Chance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class RelicHunter implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.LUCK_LOOT) {
            return 10.0f; // +10% loot
        }
        return 0.0f;
    }
}
