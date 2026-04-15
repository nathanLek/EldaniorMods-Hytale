package com.eldanior.system.skills.skills.passives.Uncommon.Endurance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class ArmoredSkin implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.ENDURANCE_DEFENSE) {
            return 10.0f; // +10 Défense d'Endurance
        }
        return 0.0f;
    }
}