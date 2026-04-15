package com.eldanior.system.skills.skills.passives.Uncommon.Regeneration;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class ManaStream implements IPassiveCombatSkill {

    @Override
    public float getRegenMultiplier(StatConfig stat) {
        if (stat == StatConfig.INTELLIGENCE) {
            return 2.0f; // x2.0 regen Mana naturelle
        }
        return 1.0f;
    }
}
