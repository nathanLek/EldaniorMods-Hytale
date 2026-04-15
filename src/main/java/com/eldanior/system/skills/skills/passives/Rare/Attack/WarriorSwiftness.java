package com.eldanior.system.skills.skills.passives.Rare.Attack;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class WarriorSwiftness implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.ATTACK_SPEED) {
            return 1.20f; // +20% vitesse d'attaque
        }
        return 1.0f;
    }
}