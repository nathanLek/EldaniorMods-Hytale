package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum AquaticData implements IMobConfig {

    // --- Petits poissons inoffensifs (Lv1-25) ---
    MINNOW("minnow", 5, 1, 15),
    CLOWNFISH("clownfish", 10, 1, 20),
    BLUEGILL("bluegill", 10, 1, 20),
    TANG_BLUE("tang blue", 12, 1, 25),
    TANG_CHEVRON("tang chevron", 12, 1, 25),
    TANG_LEMON_PEEL("tang lemon peel", 12, 1, 25),
    TANG_SAILFIN("tang sailfin", 12, 1, 25),

    // --- Poissons moyens / proies (Lv1-50) ---
    TADPOLE_RHINO("tadpole rhino", 20, 1, 30),
    SALMON("salmon", 25, 5, 40),
    TROUT_RAINBOW("trout rainbow", 25, 5, 40),
    CATFISH("catfish", 30, 10, 50),
    PUFFERFISH("pufferfish", 30, 10, 45),
    PIKE("pike", 35, 15, 50),

    // --- Crustacés (Lv5-60) ---
    CRAB("crab", 35, 5, 40),
    SHELLFISH_LAVA("shellfish lava", 45, 25, 60),
    LOBSTER("lobster", 50, 15, 50),

    // --- Méduses (Lv10-70) ---
    JELLYFISH_BLUE("jellyfish blue", 30, 10, 50),
    JELLYFISH_CYAN("jellyfish cyan", 30, 10, 50),
    JELLYFISH_GREEN("jellyfish green", 30, 10, 50),
    JELLYFISH_RED("jellyfish red", 30, 10, 50),
    JELLYFISH_YELLOW("jellyfish yellow", 30, 10, 50),
    JELLYFISH_MAN_OF_WAR("jellyfish man of war", 80, 30, 70),

    // --- Prédateurs aquatiques (Lv20-100) ---
    PIRANHA("piranha", 50, 20, 70),
    PIRANHA_BLACK("piranha black", 70, 30, 80),
    EEL_MORAY("eel moray", 90, 40, 90),
    SNAPJAW("snapjaw", 100, 40, 100),
    FROSTGILL("frostgill", 110, 50, 100),

    // --- Boss aquatique (Lv80-150) ---
    SHARK_HAMMERHEAD("shark hammerhead", 350, 80, 150);

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
