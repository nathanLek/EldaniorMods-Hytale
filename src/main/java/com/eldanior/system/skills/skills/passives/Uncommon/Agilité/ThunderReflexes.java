package com.eldanior.system.skills.skills.passives.Uncommon.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class ThunderReflexes implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.ATTACK_SPEED) {
            return 1.07f; // +7% vitesse d'attaque
        }
        return 1.0f;
    }
}