package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum TrorkData implements IMobConfig {

    // === TRORK (Lv400-490) — Tier 8 ennemi ===

    // --- Sans armes / éclaireurs (Lv400-435) ---
    TRORK_UNARMED("trork unarmed", 250, 400, 420),
    TRORK_SENTRY("trork sentry", 270, 405, 425),
    TRORK_HUNTER("trork hunter", 285, 410, 435),
    WOLF_TRORK_HUNTER("wolf trork hunter", 290, 410, 435),

    // --- Combattants (Lv415-460) ---
    TRORK_BRAWLER("trork brawler", 310, 415, 445),
    TRORK_GUARD("trork guard", 325, 420, 450),
    TRORK_MAULER("trork mauler", 340, 425, 455),
    TRORK_WARRIOR("trork warrior", 360, 430, 460),

    // --- Casters (Lv430-480) ---
    WOLF_TRORK_SHAMAN("wolf trork shaman", 370, 430, 460),
    TRORK_SHAMAN("trork shaman", 390, 440, 470),
    TRORK_DOCTOR_WITCH("trork doctor witch", 420, 450, 480),

    // --- Chef boss (Lv460-490) ---
    TRORK_CHIEFTAIN("trork chieftain", 500, 460, 490);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    TrorkData(String keyword, int xp, int minLevel, int maxLevel) {
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
