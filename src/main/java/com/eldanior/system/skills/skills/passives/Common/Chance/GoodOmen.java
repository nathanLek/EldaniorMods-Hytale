package com.eldanior.system.skills.skills.passives.Common.Chance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class GoodOmen implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.LUCK_EVENT) {
            return 15.0f; // +15% de chance d'event rare
        }
        return 0.0f;
    }
}