package com.eldanior.system.skills.skills.passives.Common.Defense;

import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

public class SturdyBody implements IPassiveCombatSkill {

    // On n'utilise pas onDefend ici ! On ajoute directement des stats au profil du joueur.
    @Override
    public float getFlatStatBonus(StatConfig stat) {
        // Ajoute +15 points bruts à l'Endurance (Défense)
        if (stat == StatConfig.ENDURANCE_DEFENSE) {
            return 15.0f;
        }
        return 0.0f;
    }
}