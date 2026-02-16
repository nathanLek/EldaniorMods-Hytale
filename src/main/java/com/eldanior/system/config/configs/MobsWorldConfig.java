package com.eldanior.system.config.configs;

import java.util.Random;

public class MobsWorldConfig {

    private static final Random RANDOM = new Random();

    // Scaling des stats par niveau
    public static final float HP_PER_LEVEL = 10.0f;      // +10 HP par niveau
    public static final float DAMAGE_PER_LEVEL = 0.5f;   // +0.5 dégât par niveau

    public static int getRandomLevel(String mobTypeId) {
        int min = MobXP.getMinLevelForId(mobTypeId);
        int max = MobXP.getMaxLevelForId(mobTypeId);

        if (max <= min) return min;
        return RANDOM.nextInt(max - min + 1) + min;
    }

    public static float getHealthBonus(int level) {
        return (level - 1) * HP_PER_LEVEL;
    }

    public static float getDamageBonus(int level) {
        return (level - 1) * DAMAGE_PER_LEVEL;
    }

    public static String getDebugInfo(String mobTypeId, int level) {
        int xp = MobXP.getXpForId(mobTypeId);
        float hpBonus = getHealthBonus(level);
        float dmgBonus = getDamageBonus(level);

        return String.format(
                "[%s] Lv.%d | XP:%d | HP:+%.0f | DMG:+%.1f",
                mobTypeId, level, xp, hpBonus, dmgBonus
        );
    }
}