package com.eldanior.system.skills.skills.passives.Divin.Agilite;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class CreatorStep implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.AGILITY_SPEED) return 1.25f;
        return 1.0f;
    }
}