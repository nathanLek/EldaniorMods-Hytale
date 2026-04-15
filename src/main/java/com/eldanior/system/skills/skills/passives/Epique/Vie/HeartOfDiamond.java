package com.eldanior.system.skills.skills.passives.Epique.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class HeartOfDiamond implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.VITALITY) return 16.0f;
        if (stat == StatConfig.ENDURANCE_DEFENSE) return 8.0f;
        return 0.0f;
    }
}