package com.eldanior.system.skills.skills.passives.Common.Endurance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class TirelessBreath implements IPassiveCombatSkill {

    private static final float ENDURANCE_BONUS = 10.0f;
    private static final float ENDURANCE_BONUS_MASTERED = 11.0f;

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.ENDURANCE) {
            return ENDURANCE_BONUS;
        }
        return 0.0f;
    }
    // Progression gérée par MovementTrackingSystem (synchro avec points endurance)
}
