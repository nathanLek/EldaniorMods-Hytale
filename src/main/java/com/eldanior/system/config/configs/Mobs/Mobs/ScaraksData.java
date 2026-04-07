package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum ScaraksData implements IMobConfig {

    SCARAK_FIGHTER_ROYAL_GUARD("scarak fighter royal guard", 70, 500, 750),
    SCARAK_BROODMOTHER("scarak broodmother", 1200, 600, 900),
    SCARAK_DEFENDER("scarak defender", 60, 450, 800),
    SCARAK_FIGHTER("scarak fighter", 45, 500, 700),
    SCARAK_SEEKER("scarak seeker", 35, 200, 500),
    SCARAK_LOUSE("scarak louse", 10, 50, 150),
    DUNGEON_SCARAK_DEFENDER("dungeon scarak defender", 65, 450, 800),
    DUNGEON_SCARAK_FIGHTER("dungeon scarak fighter", 50, 500, 700),
    DUNGEON_SCARAK_SEEKER("dungeon scarak seeker", 40, 200, 500),
    DUNGEON_SCARAK_LOUSE("dungeon scarak louse", 15, 50, 150),
    DUNGEON_SCARAK_BROODMOTHER("dungeon scarak broodmother", 1500, 600, 900),
    DUNGEON_SCARAK_BROODMOTHER_YOUNG("dungeon scarak broodmother young", 1250, 600, 900);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    ScaraksData(String keyword, int xp, int minLevel, int maxLevel) {
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