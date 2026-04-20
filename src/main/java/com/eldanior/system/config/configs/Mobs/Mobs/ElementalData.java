package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum ElementalData implements IMobConfig {

    // === ELEMENTAL / SPIRITS (Lv600-700) — Tier 11 ennemi ===
    SPIRIT_ROOT("spirit root", 600, 600, 630),
    SPIRIT_FROST("spirit frost", 680, 615, 650),
    SPIRIT_EMBER("spirit ember", 750, 635, 670),
    SPIRIT_THUNDER("spirit thunder", 850, 660, 700);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    ElementalData(String keyword, int xp, int minLevel, int maxLevel) {
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