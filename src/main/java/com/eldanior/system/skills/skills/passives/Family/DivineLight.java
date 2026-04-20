package com.eldanior.system.skills.skills.passives.Family;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

/**
 * Famille Luminara (Marquis) — Lumiere Divine
 * Bonus Mana max via multiplier sur Intelligence.
 * Nv1(lv1): +5% | Nv2(lv100): +10% | Nv3(lv300): +15% | Nv4(lv500): +20% | Nv5(lv800): +30%
 */
public class DivineLight implements IPassiveCombatSkill {

    // Le bonus sera scaling via le level, pour l'instant on retourne le bonus max
    // L'integration fine avec le level se fait via NobleFamilyModel.getActiveSkillValue()
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.INTELLIGENCE) return 1.10f;
        return 1.0f;
    }
}
