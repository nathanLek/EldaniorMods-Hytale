package com.eldanior.system.skills.skills.passives.Rare.Endurance;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class FortifiedSkin implements IPassiveCombatSkill {
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        if (stat == StatConfig.ENDURANCE_DEFENSE) return 15.0f;
        return 0.0f;
    }
}