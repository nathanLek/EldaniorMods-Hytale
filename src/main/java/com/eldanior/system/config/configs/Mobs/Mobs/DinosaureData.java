package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum DinosaureData implements IMobConfig {

    ARCHAEOPTERYX("archaeopteryx", 40, 1, 30),
    PTERODACTYL("pterodactyl", 50, 1, 30),
    RAPTOR_CAVE("raptor cave", 80, 20, 250),
    TRILLODON("trillodon", 80, 20, 250),
    TRILOBITE_BLACK("trilobite black", 50, 1, 100),
    TRILOBITE("trilobite", 50, 1, 100),
    REX_CAVE("rex_cave", 1450, 600, 999);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    DinosaureData(String keyword, int xp, int minLevel, int maxLevel) {
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