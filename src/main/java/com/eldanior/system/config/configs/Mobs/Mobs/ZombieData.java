package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum ZombieData implements IMobConfig {

    ZOMBIE_ABERRANT_BIG("zombie aberrant big", 200, 350, 500),
    ZOMBIE_ABERRANT_SMALL("zombie aberrant small", 80, 150, 280),
    ZOMBIE_ABERRANT("zombie aberrant", 250, 450, 600),
    ZOMBIE_BURNT("zombie burnt", 50, 220, 380),
    ZOMBIE_FROST("zombie frost", 40, 200, 350),
    ZOMBIE_SAND("zombie sand", 30, 200, 350),
    ZOMBIE_WEREWOLF("zombie werewolf", 50, 300, 450),
    GHOUL("ghoul", 100, 400, 550),
    HOUND_BLEACHED("hound bleached", 60, 250, 400),
    ZOMBIE_BASE("zombie", 25, 50, 150);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

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