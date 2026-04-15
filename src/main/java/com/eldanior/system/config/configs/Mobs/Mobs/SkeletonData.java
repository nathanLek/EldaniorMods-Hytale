package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum SkeletonData implements IMobConfig {

    // --- Incandescents (Elites, Lv220-380) ---
    SKELETON_INCANDESCENT_FOOTMAN("skeleton incandescent footman", 80, 220, 380),
    SKELETON_INCANDESCENT_FIGHTER("skeleton incandescent fighter", 80, 220, 380),
    SKELETON_INCANDESCENT_MAGE("skeleton incandescent mage", 80, 220, 380),
    SKELETON_INCANDESCENT_HEAD("skeleton incandescent head", 50, 180, 330),

    // --- Burnt (Mid-High, Lv180-400) ---
    SKELETON_BURNT_PRAETORIAN("skeleton burnt praetorian", 150, 280, 450),
    SKELETON_BURNT_ALCHEMIST("skeleton burnt alchemist", 70, 220, 380),
    SKELETON_BURNT_SOLDIER("skeleton burnt soldier", 60, 180, 330),
    SKELETON_BURNT_KNIGHT("skeleton burnt knight", 60, 180, 330),
    SKELETON_BURNT_LANCER("skeleton burnt lancer", 60, 180, 330),
    SKELETON_BURNT_GUNNER("skeleton burnt gunner", 50, 180, 330),
    SKELETON_BURNT_WIZARD("skeleton burnt wizard", 50, 180, 330),
    SKELETON_BURNT_ARCHER("skeleton burnt archer", 40, 150, 280),

    // --- Frost (Mid, Lv150-330) ---
    SKELETON_FROST_ARCHMAGE("skeleton frost archmage", 70, 220, 380),
    SKELETON_FROST_SOLDIER("skeleton frost soldier", 50, 180, 330),
    SKELETON_FROST_FIGHTER("skeleton frost fighter", 50, 180, 330),
    SKELETON_FROST_KNIGHT("skeleton frost knight", 50, 180, 330),
    SKELETON_FROST_RANGER("skeleton frost ranger", 40, 150, 280),
    SKELETON_FROST_ARCHER("skeleton frost archer", 40, 150, 280),
    SKELETON_FROST_MAGE("skeleton frost mage", 40, 150, 280),
    SKELETON_FROST_SCOUT("skeleton frost scout", 30, 100, 200),

    // --- Sand (Low-Mid, Lv100-250) ---
    SKELETON_SAND_SOLDIER("skeleton sand soldier", 40, 150, 280),
    SKELETON_SAND_ARCHMAGE("skeleton sand archmage", 40, 150, 280),
    SKELETON_SAND_ASSASSIN("skeleton sand assassin", 40, 150, 280),
    SKELETON_SAND_ARCHER("skeleton sand archer", 30, 100, 200),
    SKELETON_SAND_GUARD("skeleton sand guard", 30, 100, 200),
    SKELETON_SAND_MAGE("skeleton sand mage", 30, 100, 200),
    SKELETON_SAND_RANGER("skeleton sand ranger", 25, 100, 200),
    SKELETON_SAND_SCOUT("skeleton sand scout", 25, 100, 200),

    // --- Pirates (Mid, Lv180-330) ---
    SKELETON_PIRATE_STRIKER("skeleton pirate striker", 60, 180, 330),
    SKELETON_PIRATE_CAPTAIN("skeleton pirate captain", 50, 180, 330),
    SKELETON_PIRATE_GUNNER("skeleton pirate gunner", 40, 150, 280),

    // --- Basiques (Low, Lv100-250) ---
    SKELETON_ARCHMAGE("skeleton archmage", 60, 180, 330),
    SKELETON_SOLDIER("skeleton soldier", 40, 150, 280),
    SKELETON_KNIGHT("skeleton knight", 40, 150, 280),
    SKELETON_RANGER("skeleton ranger", 35, 150, 280),
    SKELETON_MAGE("skeleton mage", 35, 150, 280),
    SKELETON_SCOUT("skeleton scout", 30, 100, 200),
    SKELETON_ARCHER("skeleton archer", 25, 100, 200),
    SKELETON_FIGHTER("skeleton fighter", 25, 100, 200),
    SKELETON_BASE("skeleton", 20, 100, 200),

    RISEN_GUNNER("risen gunner", 20, 100, 200),
    RISEN_KNIGHT("risen knight", 20, 100, 200),

    // --- Dungeon (Mid, Lv150-300) ---
    DUNGEON_SKELETON_SAND_SOLDIER("dungeon skeleton sand soldier", 45, 150, 300),
    DUNGEON_SKELETON_SAND_ARCHER("dungeon skeleton sand archer", 45, 150, 300),
    DUNGEON_SKELETON_SAND_ASSASSIN("dungeon skeleton sand assassin", 45, 150, 300),
    DUNGEON_SKELETON_SAND_MAGE("dungeon skeleton sand mage", 45, 150, 300);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    SkeletonData(String keyword, int xp, int minLevel, int maxLevel) {
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