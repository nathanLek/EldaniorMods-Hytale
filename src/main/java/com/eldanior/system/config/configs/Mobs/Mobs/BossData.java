package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum BossData implements IMobConfig {

    SHADOW_KNIGHT("shadow_knight", 2000, 600, 900),
    WHALE_HUMPBACK("whale_humpback", 1500, 700, 999),
    YETI("yeti", 1650, 400, 900),
    REX_CAVE("rex_cave", 1450, 600, 999),
    WRAITH("wraith", 100, 350, 700),
    WEREWOLF("werewolf", 1300, 700, 950);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    // Constructeur standard
    BossData(String keyword, int xp, int minLevel, int maxLevel) {
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