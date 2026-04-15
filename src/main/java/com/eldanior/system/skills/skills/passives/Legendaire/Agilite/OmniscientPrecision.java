package com.eldanior.system.skills.skills.passives.Legendaire.Agilite;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class OmniscientPrecision implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.LUCK_CRITICAL) return 1.15f;
        return 1.0f;
    }
}