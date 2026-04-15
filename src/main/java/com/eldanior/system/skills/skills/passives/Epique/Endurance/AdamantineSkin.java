package com.eldanior.system.skills.skills.passives.Epique.Endurance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class AdamantineSkin implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.ENDURANCE_DEFENSE) return 20.0f;
        return 0.0f;
    }
}