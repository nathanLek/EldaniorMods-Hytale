package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum TrorkData implements IMobConfig {

    TRORK_DOCTOR_WITCH("trork doctor witch", 60, 400, 550),
    TRORK_CHIEFTAIN("trork chieftain", 100, 400, 550),
    TRORK_WARRIOR("trork warrior", 40, 300, 500),
    TRORK_SHAMAN("trork shaman", 45, 300, 500),
    TRORK_BRAWLER("trork brawler", 40, 250, 450),
    TRORK_HUNTER("trork hunter", 40, 150, 250),
    TRORK_GUARD("trork guard", 40, 200, 400),
    TRORK_MAULER("trork mauler", 40, 200, 400),
    TRORK_SENTRY("trork sentry", 40, 100, 250),
    TRORK_UNARMED("trork unarmed", 20, 120, 270);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    // Constructeur standard
    TrorkData(String keyword, int xp, int minLevel, int maxLevel) {
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