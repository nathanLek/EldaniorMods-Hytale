package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum KweebecData implements IMobConfig {

    // === KWEEBEC — Race PACIFIQUE plante (Lv80-160) ===
    KWEEBEC_SEEDLING("kweebec seedling", 15, 80, 95),
    KWEEBEC_SPROUTLING("kweebec sproutling", 20, 85, 100),
    KWEEBEC_ROOTLING("kweebec rootling", 25, 90, 110),
    KWEEBEC_SAPLING("kweebec sapling", 30, 95, 120),
    KWEEBEC_SAPLING_ORANGE("kweebec sapling orange", 35, 100, 125),
    KWEEBEC_SAPLING_PINK("kweebec sapling pink", 35, 100, 125),
    KWEEBEC_PRISONER("kweebec_prisoner", 45, 110, 140),
    KWEEBEC_ELDER("kweebec elder", 70, 130, 160),
    KWEEBEC_RAZORLEAF("kweebec razorleaf", 90, 140, 160);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    KweebecData(String keyword, int xp, int minLevel, int maxLevel) {
        this.keyword = keyword;
        this.xp = xp;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.isInvincible = false;
        this.customTitle = null;
    }

    @Override public String getKeyword() { return keyword; }
    @Override public int getXp() { return xp; }
    @Override public int getMinLevel() { return minLevel; }
    @Override public int getMaxLevel() { return maxLevel; }
    @Override public boolean isInvincible() { return isInvincible; }
    @Override public String getCustomTitle() { return customTitle; }
}