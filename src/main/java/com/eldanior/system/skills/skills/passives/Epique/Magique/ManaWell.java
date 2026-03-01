package com.eldanior.system.skills.skills.passives;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class ManaWell implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.INTELLIGENCE) { // (Ou MANA selon comment tu l'as nommé)
            return 2.30f; // +30%
        }
        return 1.0f;
    }
}
