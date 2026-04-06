package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum GolemData implements IMobConfig {

    GOLEM_GUARDIAN_VOID("golem guardian void", 1400, 800, 999),
    GOLEM_CRYSTAL_EARTH("golem crystal earth", 150, 300, 800),
    GOLEM_CRYSTAL_FLAME("golem crystal flame", 200, 300, 800),
    GOLEM_CRYSTAL_FROST("golem crystal frost", 180, 300, 800),
    GOLEM_CRYSTAL_SAND("golem crystal sand", 160, 300, 800),
    GOLEM_CRYSTAL_THUNDER("golem crystal thunder", 160, 300, 800),
    GOLEM_FIRESTEEL("golem firesteel", 180, 300, 800);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    // Constructeur standard
    GolemData(String keyword, int xp, int minLevel, int maxLevel) {
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