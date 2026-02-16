package com.eldanior.system.Leveling.systems;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.StatConfig;

import java.util.Random;

public class LuckSystem {

    private static final Random random = new Random();

    public static boolean isCriticalHit(PlayerLevelData data) {
        if (data == null) return false;
        ClassModel model = ClassManager.get(data.getPlayerClassId());
        float critChance = StatConfig.LUCK_CRITICAL.getFinalValue(data, model);
        return random.nextFloat() * 100 < critChance;
    }

    public static float getLootQualityBonus(PlayerLevelData data) {
        if (data == null) return 0.0f;
        ClassModel model = ClassManager.get(data.getPlayerClassId());
        return StatConfig.LUCK_LOOT.getFinalValue(data, model);
    }

    public static boolean rollRareEvent(PlayerLevelData data, float difficulty) {
        if (data == null) return false;
        ClassModel model = ClassManager.get(data.getPlayerClassId());
        float playerBonus = StatConfig.LUCK_EVENT.getFinalValue(data, model);
        float roll = random.nextFloat() * 100;

        return roll < (difficulty + playerBonus);
    }
}