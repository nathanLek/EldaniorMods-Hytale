package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum GoblinData implements IMobConfig {

    GOBLIN_OGRE("goblin_ogre", 100, 200, 500),
    GOBLIN_DUKE_PHASE_2("goblin_duke_phase_2", 1750, 400, 999),
    GOBLIN_DUKE_PHASE_3_FAST("goblin_duke_phase_3_fast", 1600, 400, 999),
    GOBLIN_DUKE_PHASE_3_SLOW("goblin_duke_phase_3_slow", 1600, 400, 999),
    GOBLIN_DUKE("goblin_duke", 1500, 100, 150),
    GOBLIN_SCAVENGER_BATTLEAXE("goblin_scavenger_battleaxe", 40, 25, 75),
    GOBLIN_SCAVENGER_SWORD("goblin_scavenger_sword", 40, 25, 75),
    GOBLIN_SCAVENGER("goblin_scavenger", 35, 25, 75),
    GOBLIN_LOBBER("goblin_lobber", 35, 25, 75),
    GOBLIN_LOBBER_PATROL("goblin_lobber_patrol", 35, 25, 75),
    GOBLIN_SCRAPPER("goblin_scrapper", 30, 25, 75),
    GOBLIN_MINER("goblin_miner", 30, 25, 75),
    GOBLIN_MINER_PATROL("goblin_miner_patrol", 30, 25, 75),
    GOBLIN_HERMIT("goblin_hermit", 30, 25, 75),
    GOBLIN_THIEF("goblin_thief", 35, 25, 75),
    GOBLIN_THIEF_PATROL("goblin_thief_patrol", 35, 25, 75);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    // Constructeur standard
    GoblinData(String keyword, int xp, int minLevel, int maxLevel) {
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