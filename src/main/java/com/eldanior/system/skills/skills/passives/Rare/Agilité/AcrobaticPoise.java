package com.eldanior.system.skills.skills.passives.Rare.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class AcrobaticPoise implements IPassiveCombatSkill {
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.AGILITY_JUMP) return 1.25f;
        return 1.0f;
    }
}