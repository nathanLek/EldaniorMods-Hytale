package com.eldanior.system.config.configs.Mobs.Mobs;

import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum NpcData implements IMobConfig {

    BANK_NPC("bank_npc", "Banquier", true),
    FORGERON_TIER1("forgeron_tier1_merchant", "Forgeron", true),
    ALCHEMIST_NPC("Alchemist_Npc", "Alchimiste", true),
    NOMAD_NPC("Nomad_Npc", "Nomad", true),
    TAVERNIER_NPC("Tavernier_Npc", "Tavernier", true),
    FERMIER_NPC("Fermier_Npc", "Fermier", true),
    MINEUR_NPC("Mineur_Npc", "Mineur", true),
    FORESTIER_NPC("Forestier_Npc", "Forestier", true),
    GUILDE_NPC("Guild_Npc", "Guilde", true),
    ELEVEUR_NPC("Eleveur_Npc", "Eleveuse", true),

    MARCHANT1_NPC("Marchant1_Npc", "Marchant", true),
    MARCHANT2_NPC("Marchant2_Npc", "Marchant", true),
    MARCHANT3_NPC("Marchant3_Npc", "Marchant", true),

    // PNJ de quetes
    ANCIEN_CONSEILLER_NPC("AncienConseiller_Npc", "Ancien Conseiller", true),
    TAVERNIER_QUEST_NPC("TavernierQuest_Npc", "Alex Silford", true);

    private final String keyword;
    private final int xp;
    private final int minLevel;
    private final int maxLevel;
    private final boolean isInvincible;
    private final String customTitle;

    // Constructeur 1 (Standard pour les monstres)
    NpcData(String keyword, int xp, int minLevel, int maxLevel) {
        this.keyword = keyword;
        this.xp = xp;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.isInvincible = false;
        this.customTitle = null;
    }

    // Constructeur 2 (Spécial pour les NPCs pacifiques)
    NpcData(String keyword, String customTitle, boolean isInvincible) {
        this.keyword = keyword;
        this.customTitle = customTitle;
        this.isInvincible = isInvincible;
        // On met les valeurs de combat à 0 puisqu'ils sont invincibles
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