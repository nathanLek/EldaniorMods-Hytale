package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum GolemData implements IMobConfig {

    // === GOLEM (Lv670-770) — Tier 12 ennemi ===
    GOLEM_CRYSTAL_EARTH("golem crystal earth", 800, 670, 700),
    GOLEM_CRYSTAL_SAND("golem crystal sand", 850, 685, 715),
    GOLEM_CRYSTAL_THUNDER("golem crystal thunder", 870, 695, 725),
    GOLEM_CRYSTAL_FROST("golem crystal frost", 900, 710, 740),
    GOLEM_FIRESTEEL("golem firesteel", 950, 725, 755),
    GOLEM_CRYSTAL_FLAME("golem crystal flame", 1000, 740, 770);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

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
