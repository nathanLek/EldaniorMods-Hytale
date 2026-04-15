package com.eldanior.system.skills.skills.passives.Unique.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class CosmicReflexes implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.ATTACK_SPEED) return 1.18f;
        return 1.0f;
    }
}