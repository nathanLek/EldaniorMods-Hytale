package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum OtherMobData implements IMobConfig {

    TULUK_FISHERMAN("tuluk fisherman", 80, 50, 200),
    TULUK("tuluk", 80, 50, 200),
    KLOPS_GENTLEMAN("klops gentleman", 60, 50, 200),
    KLOPS_MINER("klops miner", 60, 50, 200),
    KLOPS("klops", 60, 50, 200),
    WRAITH_LANTERN("wraith lantern", 50, 100, 500),
    EMBERWULF("emberwulf", 120, 20, 250),
    SPARK_LIVING("spark living", 50, 100, 500),
    BRAMBLEKIN_SHAMAN("bramblekin shaman", 60, 20, 250),
    BRAMBLEKIN("bramblekin", 50, 20, 250),
    GROOBLE("grooble", 50, 20, 250),
    MUSHEE("mushee", 50, 20, 250),
    QUEST_MASTER("quest master", 0, 50, 200);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    OtherMobData(String keyword, int xp, int minLevel, int maxLevel) {
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