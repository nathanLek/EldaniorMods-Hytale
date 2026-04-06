package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum FeranData implements IMobConfig {

    FERAN_WINDWALKER("feran_windwalker", 50, 50, 200),
    FERAN_SHARPTOOTH("feran_sharptooth", 50, 50, 200),
    FERAN_LONGTOOTH("feran_longtooth", 50, 50, 200),
    FERAN_BURROWER("feran_burrower", 45, 50, 200),
    FERAN_CIVILIAN("feran_civilian", 35, 50, 200),
    FERAN_CUB("feran_cub", 15, 50, 200);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    // Constructeur standard
    FeranData(String keyword, int xp, int minLevel, int maxLevel) {
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