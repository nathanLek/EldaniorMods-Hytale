package com.eldanior.system.skills.skills.passives.Common.Agilitе́;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class EldaniorSuppleness implements IPassiveCombatSkill {

    private static final float JUMP_BONUS = 1.10f;          // +10% hauteur de saut
    private static final float JUMP_BONUS_MASTERED = 1.11f;  // +11% si maîtrisé

    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.AGILITY_JUMP) {
            return JUMP_BONUS;
        }
        return 1.0f;
    }
}
