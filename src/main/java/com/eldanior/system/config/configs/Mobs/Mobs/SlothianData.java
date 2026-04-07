package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum SlothianData implements IMobConfig {

    SLOTHIAN_ELDER("slothian elder", 300, 200, 400),
    SLOTHIAN_KID("slothian kid", 300, 200, 400),
    SLOTHIAN_MONK("slothian monk", 300, 200, 400),
    SLOTHIAN_SCOUT("slothian scout", 300, 200, 400),
    SLOTHIAN_WARRIOR("slothian warrior", 300, 200, 400),
    SLOTHIAN_VILLAGER("slothian villager", 300, 200, 400),
    SLOTHIAN("slothian", 300, 200, 400);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    SlothianData(String keyword, int xp, int minLevel, int maxLevel) {
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