package com.eldanior.system.skills.skills.passives.Uncommon.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class EnrichedBlood implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.VITALITY) {
            return 6.0f; // +6 Vitalité
        }
        return 0.0f;
    }

    @Override
    public float getRegenMultiplier(StatConfig stat) {
        if (stat == StatConfig.VITALITY) {
            return 1.20f; // +20% regen vie
        }
        return 1.0f;
    }
}
