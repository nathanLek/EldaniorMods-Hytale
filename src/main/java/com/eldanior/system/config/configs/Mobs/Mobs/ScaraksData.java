package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum ScaraksData implements IMobConfig {

    // === SCARAKS (Lv800-880) — Tier 14 ennemi ===

    // --- Larves (Lv800-815) ---
    SCARAK_LOUSE("scarak louse", 1500, 800, 815),
    DUNGEON_SCARAK_LOUSE("dungeon scarak louse", 1550, 800, 815),

    // --- Éclaireurs (Lv810-830) ---
    SCARAK_SEEKER("scarak seeker", 1700, 810, 830),
    DUNGEON_SCARAK_SEEKER("dungeon scarak seeker", 1750, 810, 830),

    // --- Combattants (Lv820-845) ---
    SCARAK_FIGHTER("scarak fighter", 1900, 820, 845),
    DUNGEON_SCARAK_FIGHTER("dungeon scarak fighter", 1950, 820, 845),

    // --- Tanks (Lv830-855) ---
    SCARAK_DEFENDER("scarak defender", 2050, 830, 855),
    DUNGEON_SCARAK_DEFENDER("dungeon scarak defender", 2100, 830, 855),

    // --- Élite garde royale (Lv845-870) ---
    SCARAK_FIGHTER_ROYAL_GUARD("scarak fighter royal guard", 2300, 845, 870),

    // --- Mini-boss & boss reines (Lv855-880) ---
    DUNGEON_SCARAK_BROODMOTHER_YOUNG("dungeon scarak broodmother young", 2600, 855, 875),
    SCARAK_BROODMOTHER("scarak broodmother", 2900, 865, 880),
    DUNGEON_SCARAK_BROODMOTHER("dungeon scarak broodmother", 3000, 865, 880);

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
