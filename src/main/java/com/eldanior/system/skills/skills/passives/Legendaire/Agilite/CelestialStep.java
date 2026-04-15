package com.eldanior.system.skills.skills.passives.Legendaire.Agilite;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class CelestialStep implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.AGILITY_SPEED) return 1.20f;
        return 1.0f;
    }
}