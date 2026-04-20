package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum OutlandersData implements IMobConfig {

    // === OUTLANDER (Lv460-560) — Tier 9 ennemi ===

    // --- Sous-fifres (Lv460-505) ---
    OUTLANDER_PEON("outlander peon", 350, 460, 485),
    OUTLANDER_CULTIST("outlander cultist", 380, 465, 490),
    OUTLANDER_STALKER("outlander stalker", 410, 470, 500),
    OUTLANDER_HUNTER("outlander hunter", 430, 475, 505),

    // --- Casters (Lv480-525) ---
    WOLF_OUTLANDER_PRIEST("wolf outlander priest", 450, 480, 510),
    WOLF_OUTLANDER_SORCERER("wolf outlander sorcerer", 460, 480, 510),
    OUTLANDER_PRIEST("outlander priest", 480, 490, 520),
    OUTLANDER_SORCERER("outlander sorcerer", 490, 495, 525),

    // --- Combattants élites (Lv505-545) ---
    OUTLANDER_MARAUDER("outlander marauder", 510, 505, 535),
    OUTLANDER_BERSERKER("outlander berserker", 540, 515, 545),

    // --- Boss (Lv530-560) ---
    OUTLANDER_BRUTE("outlander brute", 600, 530, 560);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    OutlandersData(String keyword, int xp, int minLevel, int maxLevel) {
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