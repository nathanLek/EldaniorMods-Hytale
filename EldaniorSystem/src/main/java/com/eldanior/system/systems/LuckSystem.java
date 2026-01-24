package com.eldanior.system.systems;

import com.eldanior.system.components.PlayerLevelData;

import java.util.Random;

/**
 * Ce système centralise tous les calculs liés à la CHANCE (Luck).
 * Il est appelé par les autres systèmes (Combat, Interaction, Loot).
 */
public class LuckSystem {

    private static final Random random = new Random();

    // --- 1. COUP CRITIQUE (Pour le Combat) ---
    /**
     * Vérifie si un joueur effectue un coup critique.
     * Formule : 1 point de Chance = 0.5% de critique.
     * @param data Les données du joueur.
     * @return true si c'est un critique.
     */
    public static boolean isCriticalHit(PlayerLevelData data) {
        if (data == null) return false;

        // Objectif : 80% au lvl 999 (3000 pts)
        // 3000 * 0.027 = 81%
        float critChance = data.getLuck() * 0.027f;

        // Cap absolu à 80% (Demande client)
        if (critChance > 80.0f) critChance = 80.0f;

        return random.nextFloat() * 100 < critChance;
    }

    // --- 2. QUALITÉ DU BUTIN (Pour le Futur - Loot) ---
    /**
     * Calcule un bonus de qualité pour les drops ou coffres.
     * Exemple d'usage futur : +10% de chance d'avoir un item rare.
     */
    public static float getLootQualityBonus(PlayerLevelData data) {
        if (data == null) return 0.0f;
        // 10 Chance = +5% de qualité de loot
        return data.getLuck() * 0.5f;
    }

    // --- 3. DÉCOUVERTE RARE (Pour le Futur - Exploration) ---
    /**
     * Tente de déclencher un événement rare (ex: trouver un passage secret).
     * @param difficulty Difficulté de l'événement (0 à 100).
     */
    public static boolean rollRareEvent(PlayerLevelData data, float difficulty) {
        if (data == null) return false;

        float roll = random.nextFloat() * 100;
        // La chance aide à réduire la difficulté du jet
        float playerBonus = data.getLuck() * 0.2f;

        return roll < (difficulty + playerBonus);
    }
}