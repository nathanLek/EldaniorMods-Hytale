package com.eldanior.system.skills.skills.passives.Uncommon.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class OverflowingLife implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.VITALITY) {
            return 1.10f; // +10% vie max
        }
        return 1.0f;
    }
}
