package com.eldanior.system.skills.skills.passives.Uncommon.Chance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class FatedOmen implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.LUCK_EVENT) {
            return 25.0f; // +25% event rare
        }
        return 0.0f;
    }
}
