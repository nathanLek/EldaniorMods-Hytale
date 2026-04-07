package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum AquaticData implements IMobConfig {

    SHARK_HAMMERHEAD("shark hammerhead", 1000, 1, 100),
    JELLYFISH_MAN_OF_WAR("jellyfish man of war", 60, 1, 100),
    JELLYFISH_BLUE("jellyfish blue", 30, 1, 100),
    JELLYFISH_CYAN("jellyfish cyan", 30, 1, 100),
    JELLYFISH_GREEN("jellyfish green", 30, 1, 100),
    JELLYFISH_RED("jellyfish red", 30, 1, 100),
    JELLYFISH_YELLOW("jellyfish yellow", 30, 1, 100),
    EEL_MORAY("eel moray", 60, 1, 100),
    SNAPJAW("snapjaw", 60, 1, 100),
    PIKE("pike", 30, 1, 100),
    SALMON("salmon", 20, 1, 100),
    TROUT_RAINBOW("trout rainbow", 20, 1, 100),
    CATFISH("catfish", 20, 1, 100),
    BLUEGILL("bluegill", 20, 1, 100),
    FROSTGILL("frostgill", 50, 1, 100),
    PIRANHA_BLACK("piranha black", 30, 1, 100),
    PIRANHA("piranha", 25, 1, 100),
    PUFFERFISH("pufferfish", 25, 1, 100),
    CLOWNFISH("clownfish", 15, 1, 100),
    TANG_BLUE("tang blue", 15, 1, 100),
    TANG_CHEVRON("tang chevron", 15, 1, 100),
    TANG_LEMON_PEEL("tang lemon peel", 15, 1, 100),
    TANG_SAILFIN("tang sailfin", 15, 1, 100),
    LOBSTER("lobster", 40, 1, 100),
    CRAB("crab", 30, 1, 100),
    TADPOLE_RHINO("tadpole rhino", 30, 1, 100),
    SHELLFISH_LAVA("shellfish lava", 20, 1, 100),
    MINNOW("minnow", 5, 1, 100);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    AquaticData(String keyword, int xp, int minLevel, int maxLevel) {
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