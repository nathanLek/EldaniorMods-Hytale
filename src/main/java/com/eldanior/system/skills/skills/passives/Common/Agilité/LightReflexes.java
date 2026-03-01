package com.eldanior.system.skills.skills.passives.Common.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class LightReflexes implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.ATTACK_SPEED) {
            return 1.04f; // +4% de vitesse d'attaque
        }
        return 1.0f;
    }
}