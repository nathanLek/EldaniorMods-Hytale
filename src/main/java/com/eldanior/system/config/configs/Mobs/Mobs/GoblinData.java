package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum GoblinData implements IMobConfig {

    // ===========================================================
    // === GOBLIN (Lv40-110) — Tier 2 ennemi
    // ===========================================================

    // --- Patrouilles (Lv40-65) ---
    GOBLIN_LOBBER_PATROL("goblin lobber patrol", 30, 40, 60),
    GOBLIN_MINER_PATROL("goblin miner patrol", 30, 40, 60),
    GOBLIN_THIEF_PATROL("goblin thief patrol", 35, 45, 65),

    // --- Travailleurs / éclaireurs (Lv45-80) ---
    GOBLIN_MINER("goblin miner", 40, 45, 70),
    GOBLIN_SCAVENGER("goblin scavenger", 45, 50, 75),
    GOBLIN_LOBBER("goblin lobber", 50, 50, 75),
    GOBLIN_THIEF("goblin thief", 55, 55, 80),
    GOBLIN_SCRAPPER("goblin scrapper", 55, 55, 80),

    // --- Combattants armés (Lv60-90) ---
    GOBLIN_SCAVENGER_SWORD("goblin scavenger sword", 60, 60, 85),
    GOBLIN_SCAVENGER_BATTLEAXE("goblin scavenger battleaxe", 70, 65, 90),

    // --- Spécialistes (Lv70-95) ---
    GOBLIN_HERMIT("goblin hermit", 80, 70, 95),

    // --- Élites / mini-boss (Lv90-110) ---
    GOBLIN_OGRE("goblin ogre", 200, 90, 110),
    GOBLIN_DUKE("goblin duke", 350, 95, 110),
    GOBLIN_DUKE_PHASE_2("goblin duke phase 2", 400, 100, 110),
    GOBLIN_DUKE_PHASE_3_FAST("goblin duke phase 3 fast", 450, 100, 110),
    GOBLIN_DUKE_PHASE_3_SLOW("goblin duke phase 3 slow", 450, 100, 110);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    GoblinData(String keyword, int xp, int minLevel, int maxLevel) {
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
