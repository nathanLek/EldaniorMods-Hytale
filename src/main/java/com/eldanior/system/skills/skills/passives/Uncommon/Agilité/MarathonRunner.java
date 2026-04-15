package com.eldanior.system.skills.skills.passives.Uncommon.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class MarathonRunner implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.AGILITY_SPEED) {
            return 1.08f; // +8% vitesse de sprint
        }
        return 1.0f;
    }

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.ENDURANCE) {
            return 10.0f; // +10 Endurance max
        }
        return 0.0f;
    }
}