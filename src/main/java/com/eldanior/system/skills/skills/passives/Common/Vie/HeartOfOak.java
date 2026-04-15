package com.eldanior.system.skills.skills.passives.Common.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class HeartOfOak implements IPassiveCombatSkill {

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.VITALITY) {
            return 4.0f; // +4 à la Vitalité
        }
        if (stat == StatConfig.ENDURANCE_DEFENSE) {
            return 2.0f; // +2 à la Défense d'Endurance
        }
        return 0.0f;
    }
}