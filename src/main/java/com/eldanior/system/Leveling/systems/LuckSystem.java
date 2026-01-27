package com.eldanior.system.Leveling.systems;

import com.eldanior.system.Leveling.components.PlayerLevelData;

import java.util.Random;

public class LuckSystem {

    private static final Random random = new Random();

    public static boolean isCriticalHit(PlayerLevelData data) {
        if (data == null) return false;
        float critChance = data.getLuck() * 0.027f;
        if (critChance > 80.0f) critChance = 80.0f;
        return random.nextFloat() * 100 < critChance;
    }

    public static float getLootQualityBonus(PlayerLevelData data) {
        if (data == null) return 0.0f;
        // 10 Chance = +5% de qualité de loot
        return data.getLuck() * 0.5f;
    }

    public static boolean rollRareEvent(PlayerLevelData data, float difficulty) {
        if (data == null) return false;
        float roll = random.nextFloat() * 100;
        float playerBonus = data.getLuck() * 0.2f;
        return roll < (difficulty + playerBonus);
    }
}