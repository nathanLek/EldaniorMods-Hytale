package com.eldanior.system.skills.skills.passives.Common.Agilité;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class WindStep implements IPassiveCombatSkill {

    private static final float SPEED_BONUS = 1.03f;           // +3% vitesse deplacement
    private static final float SPEED_BONUS_MASTERED = 1.033f;  // +3.3% si maîtrisé

    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.AGILITY_SPEED) {
            return SPEED_BONUS;
        }
        return 1.0f;
    }
    // Progression gerée par MovementTrackingSystem (distance parcourue)
}
