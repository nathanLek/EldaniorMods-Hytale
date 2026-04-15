package com.eldanior.system.skills.skills.passives.Rare.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class BurstingLife implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.VITALITY) return 1.15f;
        return 1.0f;
    }
}