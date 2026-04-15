package com.eldanior.system.skills.skills.passives.Divin.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class HeartOfCreation implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.VITALITY) return 30.0f;
        if (stat == StatConfig.ENDURANCE_DEFENSE) return 15.0f;
        return 0.0f;
    }
}