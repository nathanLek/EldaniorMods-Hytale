package com.eldanior.system.config.configs.Mobs.Mobs;

import com.eldanior.system.config.configs.Mobs.IMobConfig;

public enum NpcData implements IMobConfig {



    // Gardes de zone (guards/GuardManager) : mortels, niveau fixe, reapparaissent a leur poste ; aucun XP (tuer un garde est un crime)
    GARDE_VILLE("Eldanior_Guard_Ville", "Garde de ville", 1000, 300),
    GARDE_ROYAL("Eldanior_Guard_Royal", "Garde royal", 5000, 500),
    GARDE_CHEVALIER("Eldanior_Guard_Chevalier", "Chevalier", 10000, 700),
    GARDE_PATROUILLEUR("Eldanior_Guard_Patrouilleur", "Patrouilleur", 0, 150),
    GARDE_MAGE_ROYAL("Eldanior_Guard_Mage", "Mage royal", 0, 600),
    GARDE_ASSASSIN("Eldanior_Guard_Assassin", "Assassin", 0, 450),
    GARDE_DISCIPLE("Eldanior_Guard_Disciple", "Disciple", 0, 120),

    // PNJ de poste (guards/GuardManager) : chevalier errant, villageois, aventurier (recrutable et compagnon), chasseur de prime, criminel
    PNJ_CHEVALIER_ERRANT("Eldanior_Npc_Chevalier_Errant", "Chevalier errant", 0, 400),
    PNJ_VILLAGEOIS("Eldanior_Npc_Villageois", "Villageois", 0, 10),
    PNJ_PELERIN("Eldanior_Npc_Pelerin", "Pèlerin", 0, 15),
    PNJ_AVENTURIER("Eldanior_Npc_Aventurier", "Aventurier", 0, 200),
    PNJ_CHASSEUR_PRIME("Eldanior_Npc_Chasseur_Prime", "Chasseur de prime", 0, 300),
    PNJ_CRIMINEL("Eldanior_Criminel", "Criminel", 300, 300),                    // XP : tuer un criminel est utile
    PNJ_MAITRE_GUILDE("Eldanior_Npc_Maitre_Guilde", "Maître de guilde", true),   // pacifique, invincible
    PNJ_MARCHAND_LUO("Eldanior_Npc_Marchand_Luo", "Marchand Luo", true),         // marchand du Clan Luo, invincible
    PNJ_COMMERCANT("Eldanior_Npc_Commercant", "Commerçant", true),            // metiers : banquier, intendant, alchimiste... invincible
    PNJ_FORGERON_LEGENDAIRE("Eldanior_Npc_Forgeron_Legendaire", "Forgeron légendaire", true),
    PNJ_SENSEI("Eldanior_Npc_Sensei", "Sensei", true),                                            // un par famille (2026-11-10)   // Humain (Luo), Nain, Elfe : fer divin
    PNJ_MARCHAND_AMBULANT("Eldanior_Npc_Marchand_Ambulant", "Marchand ambulant", true),   // vend un peu de tout, invincible

    // PNJ de quetes (norme : Quest_Npc_{Categorie}_{Type}_N{numero})
    QUEST_NPC_SECONDAIRE_MULTIPLE_N1("Quest_Npc_Secondaire_Multiple_N1", "Ancien Conseiller", true),
    QUEST_NPC_SECONDAIRE_INDICE_N1("Quest_Npc_Secondaire_Indice_N1", "Alex Silford", true);

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

    // Constructeur 3 (PNJ combattants nommes : niveau fixe, nom affiche avec le niveau)
    NpcData(String keyword, String customTitle, int xp, int level) {
        this.keyword = keyword;
        this.customTitle = customTitle;
        this.isInvincible = false;
        this.xp = xp;
        this.minLevel = level;
        this.maxLevel = level;
    }

    @Override public String getKeyword() { return keyword; }
    @Override public int getXp() { return xp; }
    @Override public int getMinLevel() { return minLevel; }
    @Override public int getMaxLevel() { return maxLevel; }
    @Override public boolean isInvincible() { return isInvincible; }
    @Override public String getCustomTitle() { return customTitle; }
}