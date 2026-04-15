package com.eldanior.system.skills.skills.passives.Uncommon.Attack;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class CombatantSwiftness implements IPassiveCombatSkill {

    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.ATTACK_SPEED) {
            return 1.15f; // +15% de vitesse d'attaque
        }
        return 1.0f;
    }
}
