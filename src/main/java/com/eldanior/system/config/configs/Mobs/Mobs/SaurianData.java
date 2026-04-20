package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum SaurianData implements IMobConfig {

    // === SAURIAN (Lv90-170) — Tier 3 ennemi ===
    SAURIAN("saurian", 70, 90, 130),
    SAURIAN_HUNTER("saurian hunter", 80, 100, 145),
    SAURIAN_ROGUE("saurian rogue", 90, 110, 160),
    SAURIAN_WARRIOR("saurian warrior", 100, 120, 170);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    SaurianData(String keyword, int xp, int minLevel, int maxLevel) {
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
