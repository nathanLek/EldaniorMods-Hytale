package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

/**
 * Charité (Gabriel) — +150% HP permanent (proportionnel aux points du joueur).
 * 50 vitalité → 125 vitalité effective. Si +2 → 52 → 130.
 * L'effet de soin d'allié est géré par DivineAuraSystem.
 * Église RELIGIEUX+ only.
 */
public class Charite implements IPassiveCombatSkill {

    // +150% HP = x2.5 (proportionnel, recalculé auto quand on ajoute des points)
    @Override
    public float getStatMultiplier(StatConfig stat) {
        if (stat == StatConfig.VITALITY) return 2.5f;
        return 1.0f;
    }
}
