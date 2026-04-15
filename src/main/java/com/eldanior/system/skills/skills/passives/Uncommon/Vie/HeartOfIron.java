package com.eldanior.system.skills.skills.passives.Uncommon.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class HeartOfIron implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.VITALITY) {
            return 8.0f; // +8 Vitalité
        }
        if (stat == StatConfig.ENDURANCE_DEFENSE) {
            return 4.0f; // +4 Défense Endurance
        }
        return 0.0f;
    }
}
