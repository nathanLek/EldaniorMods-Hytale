package com.eldanior.system.skills.skills.passives.Common.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class LifeForce implements IPassiveCombatSkill {

    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.VITALITY) {
            return 1.05f; // +5% de vie maximale
        }
        return 1.0f;
    }
}