package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum VoidData implements IMobConfig {

    // === VOID (Lv530-630) — Tier 10 ennemi ===

    // --- Larves & support (Lv530-595) ---
    LARVA_VOID("larva void", 450, 530, 555),
    EYE_VOID("eye void", 480, 540, 565),
    CRAWLER_VOID("crawler void", 510, 550, 580),
    NECROMANCER_VOID("necromancer void", 540, 565, 595),

    // --- Élite spectral (Lv580-610) ---
    SPECTRE_VOID("spectre void", 590, 580, 610),

    // --- Boss invocateur (Lv600-630) ---
    SPAWN_VOID("spawn void", 700, 600, 630);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    VoidData(String keyword, int xp, int minLevel, int maxLevel) {
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
