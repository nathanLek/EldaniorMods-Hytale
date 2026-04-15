package com.eldanior.system.skills.skills.passives.Epique.Magique;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class GeniusMind implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.INTELLIGENCE) return 50.0f;
        return 0.0f;
    }
}