package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum OtherMobData implements IMobConfig {

    // === MOBS SOLITAIRES — répartis par thème ===

    // --- NPC Quêteur (invincible) ---
    QUEST_MASTER("quest master", "Maître des Quêtes", true),

    // --- Faune champignonnière (Lv5-40) ---
    GROOBLE("grooble", 15, 5, 30),
    MUSHEE("mushee", 20, 10, 40),

    // --- Plantes hostiles (Lv60-150) ---
    BRAMBLEKIN("bramblekin", 80, 60, 130),
    BRAMBLEKIN_SHAMAN("bramblekin shaman", 110, 75, 150),

    // --- Tuluks arctiques (Lv100-190) ---
    TULUK_FISHERMAN("tuluk fisherman", 150, 100, 180),
    TULUK("tuluk", 180, 110, 190),

    // --- Klops géants (Lv180-280) ---
    KLOPS_MINER("klops miner", 280, 180, 260),
    KLOPS_GENTLEMAN("klops gentleman", 300, 190, 270),
    KLOPS("klops", 350, 200, 280),

    // --- Prédateurs élémentaires (Lv250-400) ---
    EMBERWULF("emberwulf", 380, 250, 330),
    WRAITH_LANTERN("wraith lantern", 420, 280, 360),
    SPARK_LIVING("spark living", 480, 320, 400);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    // Constructeur 1 (Standard pour les monstres)
    OtherMobData(String keyword, int xp, int minLevel, int maxLevel) {
        this.keyword = keyword;
        this.xp = xp;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.isInvincible = false;
        this.customTitle = null;
    }

    // Constructeur 2 (Spécial pour les NPCs invincibles)
    OtherMobData(String keyword, String customTitle, boolean isInvincible) {
        this.keyword = keyword;
        this.customTitle = customTitle;
        this.isInvincible = isInvincible;
        this.xp = 0;
        this.minLevel = 0;
        this.maxLevel = 0;
    }

    @Override public String getKeyword() { return keyword; }
    @Override public int getXp() { return xp; }
    @Override public int getMinLevel() { return minLevel; }
    @Override public int getMaxLevel() { return maxLevel; }
    @Override public boolean isInvincible() { return isInvincible; }
    @Override public String getCustomTitle() { return customTitle; }
}
