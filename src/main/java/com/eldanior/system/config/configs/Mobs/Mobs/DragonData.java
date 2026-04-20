package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum DragonData implements IMobConfig {

    // === DRAGONS (Lv860-999) — Tier 15 ennemi (END-GAME) ===

    // --- Petit dragon (Lv860-900) ---
    SNAPDRAGON("snapdragon", 4000, 860, 900),

    // --- Dragons élémentaires (Lv920-999) ---
    DRAGON_FROST("dragon frost", 8000, 920, 999),
    DRAGON_FIRE("dragon fire", 10000, 940, 999);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    DragonData(String keyword, int xp, int minLevel, int maxLevel) {
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