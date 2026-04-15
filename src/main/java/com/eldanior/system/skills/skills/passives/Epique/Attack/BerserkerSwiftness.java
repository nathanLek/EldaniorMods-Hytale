package com.eldanior.system.skills.skills.passives.Epique.Attack;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class BerserkerSwiftness implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.ATTACK_SPEED) return 1.25f;
        return 1.0f;
    }
}
