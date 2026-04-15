package com.eldanior.system.skills.skills.passives.Legendaire.Vie;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class HeartOfGenesis implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.VITALITY) return 25.0f;
        if (stat == StatConfig.ENDURANCE_DEFENSE) return 12.0f;
        return 0.0f;
    }
}