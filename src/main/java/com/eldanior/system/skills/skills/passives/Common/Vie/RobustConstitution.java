package com.eldanior.system.skills.skills.passives.Common.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class RobustConstitution implements IPassiveCombatSkill {

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.VITALITY) {
            return 5.0f; // +5 à la Vitalité
        }
        return 0.0f;
    }
}