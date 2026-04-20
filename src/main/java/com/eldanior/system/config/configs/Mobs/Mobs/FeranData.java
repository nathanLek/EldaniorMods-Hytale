package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum FeranData implements IMobConfig {

    // === FERAN — Race PACIFIQUE (Lv80-160) ===
    FERAN_CUB("feran cub", 30, 80, 110),
    FERAN_CIVILIAN("feran civilian", 40, 85, 120),
    FERAN_BURROWER("feran burrower", 50, 90, 130),
    FERAN_LONGTOOTH("feran longtooth", 65, 100, 140),
    FERAN_SHARPTOOTH("feran sharptooth", 75, 110, 150),
    FERAN_WINDWALKER("feran windwalker", 90, 120, 160);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

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