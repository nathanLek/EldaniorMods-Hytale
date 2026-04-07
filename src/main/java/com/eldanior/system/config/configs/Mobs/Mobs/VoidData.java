package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum VoidData implements IMobConfig {

    SPAWN_VOID("spawn void", 150, 200, 500),
    SPECTRE_VOID("spectre void", 80, 200, 500),
    CRAWLER_VOID("crawler void", 60, 200, 500),
    EYE_VOID("eye void", 50, 200, 500),
    NECROMANCER_VOID("necromancer void", 50, 200, 500),
    LARVA_VOID("larva void", 20, 200, 500);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    VoidData(String keyword, int xp, int minLevel, int maxLevel) {
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