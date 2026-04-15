package com.eldanior.system.skills.skills.passives.Uncommon.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class SteelConstitution implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.VITALITY) {
            return 10.0f; // +10 Vitalité
        }
        return 0.0f;
    }
}
