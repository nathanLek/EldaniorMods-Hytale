package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum BossData implements IMobConfig {

    // --- Tier 1 — Boss bas niveau (Lv300-500) ---
    WRAITH("wraith", 1800, 350, 500),

    // --- Tier 2 — Boss mid (Lv500-700) ---
    WEREWOLF("werewolf", 2400, 500, 700),
    YETI("yeti", 2800, 550, 700),

    // --- Tier 3 — Boss haut (Lv700-850) ---
    HEDERA("hedera", 3200, 650, 800),
    WHALE_HUMPBACK("whale humpback", 3800, 700, 850),
    SHADOW_KNIGHT("shadow knight", 4000, 750, 850),

    // --- Tier 4 — End-game (Lv850-999) ---
    GOLEM_GUARDIAN_VOID("golem guardian void", 5500, 850, 999);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    BossData(String keyword, int xp, int minLevel, int maxLevel) {
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