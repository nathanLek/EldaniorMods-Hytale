package com.eldanior.system.skills.skills.passives.Legendaire.Attack;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class GodSlayerSwiftness implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.ATTACK_SPEED) return 1.35f;
        return 1.0f;
    }
}
