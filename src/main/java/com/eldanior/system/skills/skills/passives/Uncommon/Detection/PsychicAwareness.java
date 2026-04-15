package com.eldanior.system.skills.skills.passives.Uncommon.Detection;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class PsychicAwareness implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.STEALTH_DETECTION) {
            return 1.10f; // +10% détection invisibilité
        }
        return 1.0f;
    }
}