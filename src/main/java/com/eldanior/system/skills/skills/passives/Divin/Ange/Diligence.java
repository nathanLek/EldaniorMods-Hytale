package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

/**
 * Diligence (Métatron) — +20% XP, progression des skills x5, +100% agilité.
 * Le bonus XP et progression skills sont gérés par le système XP/skills qui vérifie ce passif.
 * L'agilité est appliquée via getStatMultiplier.
 * Église RELIGIEUX+ only.
 */
public class Diligence implements IPassiveCombatSkill {

    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.AGILITY_SPEED) {
            return 2.0f; // +100% agility
        }
        return 1.0f;
    }
}
