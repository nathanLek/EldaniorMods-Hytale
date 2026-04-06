package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum ZombieData implements IMobConfig {

    ZOMBIE_ABERRANT_BIG("zombie_aberrant_big", 200, 300, 600),
    ZOMBIE_ABERRANT_SMALL("zombie_aberrant_small", 80, 100, 300),
    ZOMBIE_ABERRANT("zombie_aberrant", 250, 400, 700),
    ZOMBIE_BURNT("zombie_burnt", 50, 200, 500),
    ZOMBIE_FROST("zombie_frost", 40, 200, 500),
    ZOMBIE_SAND("zombie_sand", 30, 200, 500),
    ZOMBIE_WEREWOLF("zombie_werewolf", 50, 300, 550),
    GHOUL("ghoul", 100, 400, 600),
    ZOMBIE_BASE("zombie", 25, 50, 200);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    // Constructeur standard
    ZombieData(String keyword, int xp, int minLevel, int maxLevel) {
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