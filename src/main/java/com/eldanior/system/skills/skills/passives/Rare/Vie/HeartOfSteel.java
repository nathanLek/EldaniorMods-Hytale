package com.eldanior.system.skills.skills.passives.Rare.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class HeartOfSteel implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.VITALITY) return 12.0f;
        if (stat == StatConfig.ENDURANCE_DEFENSE) return 6.0f;
        return 0.0f;
    }
}