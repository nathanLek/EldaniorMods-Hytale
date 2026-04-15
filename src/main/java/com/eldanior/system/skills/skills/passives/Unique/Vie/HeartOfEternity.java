package com.eldanior.system.skills.skills.passives.Unique.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class HeartOfEternity implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.VITALITY) return 20.0f;
        if (stat == StatConfig.ENDURANCE_DEFENSE) return 10.0f;
        return 0.0f;
    }
}
