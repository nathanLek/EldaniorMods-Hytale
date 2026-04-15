package com.eldanior.system.skills.skills.passives.Rare.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class AncientBlood implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.VITALITY) return 9.0f;
        return 0.0f;
    }
    @Override
    public float getRegenMultiplier(StatConfig stat) {
        if (stat == StatConfig.VITALITY) return 1.30f;
        return 1.0f;
    }
}