package com.eldanior.system.skills.skills.passives.Common.Regeneration;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class ManaFont implements IPassiveCombatSkill {

    @Override
    public float getRegenMultiplier(StatConfig stat) {
        // Si la stat qui se régénère est le Mana (Intelligence)
        if (stat == StatConfig.INTELLIGENCE) {
            return 1.20f; // Multiplie la vitesse par 1.5 (passe de 3h à 1h !)
        }
        return 1.0f; // Les autres stats (Vie, Endurance) restent normales
    }
}