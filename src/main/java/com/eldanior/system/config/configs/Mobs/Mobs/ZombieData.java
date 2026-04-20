package com.eldanior.system.config.configs.Mobs.Mobs;


import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum ZombieData implements IMobConfig {

    // === ZOMBIE (Lv330-420) — Tier 7 ennemi ===

    // --- Basique & variantes élémentaires (Lv330-380) ---
    ZOMBIE_BASE("zombie", 200, 330, 360),
    ZOMBIE_SAND("zombie sand", 220, 335, 370),
    ZOMBIE_FROST("zombie frost", 230, 340, 375),
    ZOMBIE_BURNT("zombie burnt", 240, 345, 380),

    // --- Mutants & meutes (Lv350-400) ---
    ZOMBIE_ABERRANT_SMALL("zombie aberrant small", 270, 350, 385),
    HOUND_BLEACHED("hound bleached", 280, 355, 390),
    ZOMBIE_WEREWOLF("zombie werewolf", 310, 365, 400),

    // --- Élites (Lv375-415) ---
    ZOMBIE_ABERRANT_BIG("zombie aberrant big", 350, 375, 410),
    GHOUL("ghoul", 380, 385, 415),

    // --- Boss (Lv395-420) ---
    ZOMBIE_ABERRANT("zombie aberrant", 450, 395, 420);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    ZombieData(String keyword, int xp, int minLevel, int maxLevel) {
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
