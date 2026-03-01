package com.eldanior.system.skills.skills.passives.Common.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class WindStep implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.AGILITY_SPEED) {
            return 1.03f; // +3% de vitesse de déplacement globale
        }
        return 1.0f;
    }
}