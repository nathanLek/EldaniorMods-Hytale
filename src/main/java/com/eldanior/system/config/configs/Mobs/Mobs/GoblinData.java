package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum GoblinData implements IMobConfig {


    // === GOBLIN LVL 45 / 75 - BOSS LVL 100 / 300 ===
    GOBLIN_OGRE("goblin ogre", 1000, 100, 150),
    GOBLIN_DUKE_PHASE_2("goblin duke phase 2", 1750, 250, 300),
    GOBLIN_DUKE_PHASE_3_FAST("goblin duke phase 3 fast", 1750, 250, 300),
    GOBLIN_DUKE_PHASE_3_SLOW("goblin duke phase 3 slow", 1750, 250, 300),
    GOBLIN_DUKE("goblin duke", 1750, 150, 200),
    GOBLIN_SCAVENGER_BATTLEAXE("goblin scavenger battleaxe", 100, 45, 75),
    GOBLIN_SCAVENGER_SWORD("goblin scavenger sword", 100, 45, 75),
    GOBLIN_SCAVENGER("goblin scavenger", 100, 45, 75),
    GOBLIN_LOBBER("goblin lobber", 100, 45, 75),
    GOBLIN_LOBBER_PATROL("goblin lobber patrol", 45, 25, 75),
    GOBLIN_SCRAPPER("goblin scrapper", 100, 45, 75),
    GOBLIN_MINER("goblin miner", 100, 45, 75),
    GOBLIN_MINER_PATROL("goblin miner patrol", 45, 75, 75),
    GOBLIN_HERMIT("goblin hermit", 100, 45, 75),
    GOBLIN_THIEF("goblin thief", 100, 45, 75),
    GOBLIN_THIEF_PATROL("goblin thief patrol", 100, 45, 75);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

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