package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum DinosaureData implements IMobConfig {

    // === DINOSAURES (Lv740-830) — Tier 13 ennemi ===

    // --- Petits dinos & arthropodes (Lv740-780) ---
    TRILOBITE("trilobite", 1000, 740, 760),
    ARCHAEOPTERYX("archaeopteryx", 1100, 745, 770),
    PTERODACTYL("pterodactyl", 1150, 750, 775),
    TRILOBITE_BLACK("trilobite black", 1200, 755, 780),

    // --- Prédateurs dinos (Lv770-820) ---
    RAPTOR_CAVE("raptor cave", 1500, 770, 805),
    TRILLODON("trillodon", 1700, 785, 820),

    // --- T-Rex boss (Lv805-830) ---
    REX_CAVE("rex_cave", 2200, 805, 830);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    DinosaureData(String keyword, int xp, int minLevel, int maxLevel) {
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
