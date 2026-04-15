package com.eldanior.system.skills.skills.passives.Uncommon.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class CatlikePoise implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.AGILITY_JUMP) {
            return 1.18f; // +18% hauteur de saut
        }
        return 1.0f;
    }
}