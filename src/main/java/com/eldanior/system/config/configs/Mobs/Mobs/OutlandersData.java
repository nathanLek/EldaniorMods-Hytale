package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum OutlandersData implements IMobConfig {

    OUTLANDER_BERSERKER("outlander_berserker", 70, 150, 400),
    OUTLANDER_MARAUDER("outlander_marauder", 60, 150, 400),
    OUTLANDER_PRIEST("outlander_priest", 65, 100, 200),
    OUTLANDER_SORCERER("outlander_sorcerer", 65, 100, 200),
    OUTLANDER_BRUTE("outlander_brute", 80, 200, 500),
    OUTLANDER_CULTIST("outlander_cultist", 50, 100, 350),
    OUTLANDER_HUNTER("outlander_hunter", 80, 120, 370),
    OUTLANDER_STALKER("outlander_stalker", 40, 130, 380),
    OUTLANDER_PEON("outlander_peon", 30, 90, 300);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    // Constructeur standard
    OutlandersData(String keyword, int xp, int minLevel, int maxLevel) {
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