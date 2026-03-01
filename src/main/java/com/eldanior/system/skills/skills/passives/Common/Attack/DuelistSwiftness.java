package com.eldanior.system.skills.skills.passives.Common.Attack;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class DuelistSwiftness implements IPassiveCombatSkill {

    @Override
    public float getStatMultiplier(StatConfig stat) {
        // On vérifie si la stat demandée par le système est la vitesse d'attaque
        if (stat == StatConfig.ATTACK_SPEED) {
            return 1.10f; // +10% de vitesse
        }
        return 1.0f;
    }
}