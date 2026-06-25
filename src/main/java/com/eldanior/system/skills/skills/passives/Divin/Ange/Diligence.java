package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

/**
 * Diligence (Métatron) — +20% XP, progression des skills x5, +100% agilité.
 * Le +100% agilité est permanent (proportionnel aux points du joueur).
 * Le +20% XP et x5 progression sont gérés dans DeathXPSystem et CombatStatsSystem.
 * Église RELIGIEUX+ only.
 */
public class Diligence implements IPassiveCombatSkill {

    // +100% agilité permanent = x2 (proportionnel, recalculé auto)
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.AGILITY_SPEED) return 2.0f;
        return 1.0f;
    }
}
