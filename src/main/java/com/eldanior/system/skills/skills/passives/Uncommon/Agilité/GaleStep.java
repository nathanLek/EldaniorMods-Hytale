package com.eldanior.system.skills.skills.passives.Uncommon.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class GaleStep implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.AGILITY_SPEED) {
            return 1.05f; // +5% vitesse de déplacement
        }
        return 1.0f;
    }
}