package com.eldanior.system.skills.skills.passives.Common.Endurance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class TirelessBreath implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.ENDURANCE) {
            return 10.0f; // +10 Endurance brute
        }
        return 0.0f;
    }
}