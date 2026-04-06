package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum KweebecData implements IMobConfig {

    KWEEBEC_RAZORLEAF("kweebec_razorleaf", 60, 50, 200),
    KWEEBEC_SAPLING_ORANGE("kweebec_sapling_orange", 35, 50, 200),
    KWEEBEC_SAPLING_PINK("kweebec_sapling_pink", 35, 50, 200),
    KWEEBEC_SAPLING("kweebec_sapling", 40, 50, 200),
    KWEEBEC_ROOTLING("kweebec_rootling", 35, 50, 200),
    KWEEBEC_SPROUTLING("kweebec_sproutling", 25, 50, 200),
    KWEEBEC_SEEDLING("kweebec_seedling", 15, 50, 200),
    KWEEBEC_PRISONER("kweebec_prisoner", 30, 50, 200),
    KWEEBEC_ELDER("kweebec_elder", 30, 50, 200);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    // Constructeur standard
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