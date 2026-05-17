package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum SkeletonData implements IMobConfig {

    // ===========================================================
    // === BASIC + RISEN + PIRATES (Lv1-60) — Tier 1 ennemi
    // ===========================================================
    SKELETON_BASE("skeleton", 15, 1, 30),
    RISEN_GUNNER("risen gunner", 18, 5, 35),
    RISEN_KNIGHT("risen knight", 20, 5, 35),
    SKELETON_SCOUT("skeleton scout", 18, 5, 40),
    SKELETON_ARCHER("skeleton archer", 22, 10, 45),
    SKELETON_FIGHTER("skeleton fighter", 25, 10, 45),
    SKELETON_RANGER("skeleton ranger", 28, 15, 50),
    SKELETON_MAGE("skeleton mage", 30, 15, 50),
    SKELETON_KNIGHT("skeleton knight", 32, 20, 55),
    SKELETON_SOLDIER("skeleton soldier", 35, 20, 55),
    SKELETON_ARCHMAGE("skeleton archmage", 45, 25, 60),
    SKELETON_PIRATE_GUNNER("skeleton pirate gunner", 30, 15, 50),
    SKELETON_PIRATE_STRIKER("skeleton pirate striker", 38, 25, 55),
    SKELETON_PIRATE_CAPTAIN("skeleton pirate captain", 50, 30, 60),
    SKELETON_BURNT_PRAETORIAN_PATROL("skeleton burnt praetorian patrol", 1000, 60, 100),

    // ===========================================================
    // === FROST (glace) (Lv150-230) — Tier 4 ennemi
    // ===========================================================
    SKELETON_FROST_SCOUT("skeleton frost scout", 100, 150, 180),
    SKELETON_FROST_ARCHER("skeleton frost archer", 110, 155, 190),
    SKELETON_FROST_RANGER("skeleton frost ranger", 115, 160, 195),
    SKELETON_FROST_MAGE("skeleton frost mage", 120, 165, 200),
    SKELETON_FROST_FIGHTER("skeleton frost fighter", 130, 170, 205),
    SKELETON_FROST_KNIGHT("skeleton frost knight", 140, 175, 210),
    SKELETON_FROST_SOLDIER("skeleton frost soldier", 145, 180, 215),
    SKELETON_FROST_ARCHMAGE("skeleton frost archmage", 180, 200, 230),

    // ===========================================================
    // === BURNT + INCANDESCENT (feu) (Lv210-290) — Tier 5 ennemi
    // ===========================================================
    SKELETON_BURNT_ARCHER("skeleton burnt archer", 180, 210, 240),
    SKELETON_BURNT_GUNNER("skeleton burnt gunner", 185, 215, 245),
    SKELETON_BURNT_WIZARD("skeleton burnt wizard", 195, 220, 250),
    SKELETON_BURNT_LANCER("skeleton burnt lancer", 200, 220, 250),
    SKELETON_BURNT_KNIGHT("skeleton burnt knight", 210, 225, 255),
    SKELETON_BURNT_SOLDIER("skeleton burnt soldier", 215, 225, 255),
    SKELETON_BURNT_ALCHEMIST("skeleton burnt alchemist", 230, 235, 265),
    SKELETON_BURNT_PRAETORIAN("skeleton burnt praetorian", 280, 250, 290),
    SKELETON_INCANDESCENT_HEAD("skeleton incandescent head", 200, 215, 245),
    SKELETON_INCANDESCENT_FOOTMAN("skeleton incandescent footman", 220, 225, 260),
    SKELETON_INCANDESCENT_FIGHTER("skeleton incandescent fighter", 240, 235, 270),
    SKELETON_INCANDESCENT_MAGE("skeleton incandescent mage", 260, 245, 280),

    // ===========================================================
    // === SAND + DUNGEON (antique/désert) (Lv270-360) — Tier 6 ennemi
    // ===========================================================
    SKELETON_SAND_SCOUT("skeleton sand scout", 280, 270, 300),
    SKELETON_SAND_RANGER("skeleton sand ranger", 290, 275, 305),
    SKELETON_SAND_ARCHER("skeleton sand archer", 300, 280, 310),
    SKELETON_SAND_MAGE("skeleton sand mage", 310, 285, 315),
    SKELETON_SAND_GUARD("skeleton sand guard", 320, 290, 320),
    SKELETON_SAND_SOLDIER("skeleton sand soldier", 330, 295, 330),
    SKELETON_SAND_ASSASSIN("skeleton sand assassin", 360, 305, 340),
    SKELETON_SAND_ARCHMAGE("skeleton sand archmage", 400, 320, 360),
    DUNGEON_SKELETON_SAND_ARCHER("dungeon skeleton sand archer", 320, 280, 320),
    DUNGEON_SKELETON_SAND_MAGE("dungeon skeleton sand mage", 330, 290, 325),
    DUNGEON_SKELETON_SAND_SOLDIER("dungeon skeleton sand soldier", 350, 295, 335),
    DUNGEON_SKELETON_SAND_ASSASSIN("dungeon skeleton sand assassin", 380, 305, 345);

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
