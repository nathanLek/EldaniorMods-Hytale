package com.eldanior.system.skills.skills.passives.Common.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class EldaniorSuppleness implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.AGILITY_JUMP) {
            return 1.10f; // +10% de hauteur de saut
        }
        return 1.0f;
    }
}