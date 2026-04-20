package com.eldanior.system.skills.skills.passives.Family;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;

/**
 * Famille Ashford (Marquis) — Sang du Phenix
 * Bonus PV max.
 * Nv1(lv1): +10 | Nv2(lv100): +25 | Nv3(lv300): +50 | Nv4(lv500): +80 | Nv5(lv800): +120
 */
public class PhoenixBloodFamily implements IPassiveCombatSkill {

    @Override
    public float getFlatStatBonus(StatConfig stat) {
        // Le bonus sera applique via le system de stats avec le level du joueur
        // Ici on donne le bonus de base, le scaling est gere dans NobleFamilyModel
        return 0;
    }
}