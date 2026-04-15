package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum BossData implements IMobConfig {

    GOLEM_GUARDIAN_VOID("golem guardian void", 1400, 850, 999),
    SHADOW_KNIGHT("shadow knight", 2000, 700, 850),
    WHALE_HUMPBACK("whale humpback", 1500, 750, 900),
    YETI("yeti", 1650, 500, 700),
    WRAITH("wraith", 1500, 350, 500),
    HEDERA("hedera", 1500, 500, 700),
    WEREWOLF("werewolf", 1300, 700, 850);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

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