package com.eldanior.system.skills.skills.passives.Common.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class VitalBlood implements IPassiveCombatSkill {

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.VITALITY) {
            return 3.0f; // +3 à la Vitalité
        }
        return 0.0f;
    }

    @Override
    public float getRegenMultiplier(StatConfig stat) {
        if (stat == StatConfig.VITALITY) {
            return 1.10f; // +10% de régénération de vie
        }
        return 1.0f;
    }
}