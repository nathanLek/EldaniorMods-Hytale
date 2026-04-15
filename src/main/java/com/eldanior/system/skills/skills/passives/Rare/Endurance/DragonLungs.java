package com.eldanior.system.skills.skills.passives.Rare.Endurance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class DragonLungs implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.ENDURANCE) return 30.0f;
        return 0.0f;
    }
}