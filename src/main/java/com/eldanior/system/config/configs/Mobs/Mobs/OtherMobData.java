package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum OtherMobData implements IMobConfig {

    SLOTHIAN("slothian", 120, 50, 200),
    TULUK_FISHERMAN("tuluk_fisherman", 80, 50, 200),
    KLOPS_GENTLEMAN("klops_gentleman", 60, 50, 200),
    KLOPS_MINER("klops_miner", 60, 50, 200),
    HEDERA("hedera", 150, 50, 200),
    WRAITH_LANTERN("wraith_lantern", 50, 100, 500),
    QUEST_MASTER("quest_master", 0, 50, 200);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    // Constructeur standard
    OtherMobData(String keyword, int xp, int minLevel, int maxLevel) {
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