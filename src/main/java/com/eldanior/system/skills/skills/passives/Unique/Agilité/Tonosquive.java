package com.eldanior.system.skills.skills.passives.Unique.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class Tonosquive implements IPassiveCombatSkill {

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        // ⚠️ Remplace DODGE_CHANCE par le nom exact de ta stat d'esquive dans StatConfig
        if (stat == StatConfig.DODGE_CHANCE) {
            return 30.0f; // Ajoute +30% de chance d'esquive brutes
        }
        return 0.0f;
    }
}