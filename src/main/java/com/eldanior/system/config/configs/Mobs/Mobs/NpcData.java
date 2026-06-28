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
    MENUISIER_NPC("Menuisier_Npc", "Menuisier", true),
    POISSONNIER_NPC("Poissonnier_Npc", "Poissonier", true),

    MARCHANT1_NPC("Marchant1_Npc", "Marchant", true),
    MARCHANT2_NPC("Marchant2_Npc", "Marchant", true),
    MARCHANT3_NPC("Marchant3_Npc", "Marchant", true),

    // PNJ de quetes (norme : Quest_Npc_{Categorie}_{Type}_N{numero})
    QUEST_NPC_SECONDAIRE_MULTIPLE_N1("Quest_Npc_Secondaire_Multiple_N1", "Ancien Conseiller", true),
    QUEST_NPC_SECONDAIRE_INDICE_N1("Quest_Npc_Secondaire_Indice_N1", "Alex Silford", true),

    // PNJ de quetes secondaires de chasse
    QUEST_NPC_SECONDAIRE_CHASSE_N1("Quest_Npc_Secondaire_Chasse_N1", "Rodrik le Garde", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N2("Quest_Npc_Secondaire_Chasse_N2", "Elara la Chasseuse", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N3("Quest_Npc_Secondaire_Chasse_N3", "Bjorn le Veteran", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N4("Quest_Npc_Secondaire_Chasse_N4", "Sylas l'Eclaireur", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N5("Quest_Npc_Secondaire_Chasse_N5", "Kira la Sentinelle", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N6("Quest_Npc_Secondaire_Chasse_N6", "Marcus le Chevalier", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N7("Quest_Npc_Secondaire_Chasse_N7", "Lina la Piegeuse", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N8("Quest_Npc_Secondaire_Chasse_N8", "Thorin le Nain", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N9("Quest_Npc_Secondaire_Chasse_N9", "Selene la Druide", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N10("Quest_Npc_Secondaire_Chasse_N10", "Gareth le Mercenaire", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N11("Quest_Npc_Secondaire_Chasse_N11", "Mira la Rodeuse", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N12("Quest_Npc_Secondaire_Chasse_N12", "Orik le Barbare", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N13("Quest_Npc_Secondaire_Chasse_N13", "Nessa l'Archere", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N14("Quest_Npc_Secondaire_Chasse_N14", "Aldric le Paladin", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N15("Quest_Npc_Secondaire_Chasse_N15", "Ivy la Forestiere", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N16("Quest_Npc_Secondaire_Chasse_N16", "Dante le Sombre", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N17("Quest_Npc_Secondaire_Chasse_N17", "Freya la Valkyrie", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N18("Quest_Npc_Secondaire_Chasse_N18", "Hugo le Trappeur", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N19("Quest_Npc_Secondaire_Chasse_N19", "Zara la Lame", true),
    QUEST_NPC_SECONDAIRE_CHASSE_N20("Quest_Npc_Secondaire_Chasse_N20", "Balthazar l'Ancien", true),

    // PNJ de quetes secondaires - Minage
    QUEST_NPC_SECONDAIRE_MINAGE_N1("Quest_Npc_Secondaire_Minage_N1", "Durgan le Mineur", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N2("Quest_Npc_Secondaire_Minage_N2", "Petra la Geologue", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N3("Quest_Npc_Secondaire_Minage_N3", "Magnar le Forgeron", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N4("Quest_Npc_Secondaire_Minage_N4", "Silas le Prospecteur", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N5("Quest_Npc_Secondaire_Minage_N5", "Eira la Cristalliere", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N6("Quest_Npc_Secondaire_Minage_N6", "Volkan l'Artificier", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N7("Quest_Npc_Secondaire_Minage_N7", "Ingrid la Maitre-Mine", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N8("Quest_Npc_Secondaire_Minage_N8", "Gorm le Tailleur", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N9("Quest_Npc_Secondaire_Minage_N9", "Helga la Naine", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N10("Quest_Npc_Secondaire_Minage_N10", "Rolf le Dynamiteur", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N11("Quest_Npc_Secondaire_Minage_N11", "Astrid la Cartographe", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N12("Quest_Npc_Secondaire_Minage_N12", "Finn le Terrassier", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N13("Quest_Npc_Secondaire_Minage_N13", "Olga la Fondatrice", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N14("Quest_Npc_Secondaire_Minage_N14", "Bram le Sculpteur", true),
    QUEST_NPC_SECONDAIRE_MINAGE_N15("Quest_Npc_Secondaire_Minage_N15", "Thane l'Excavateur", true),

    // PNJ de quetes secondaires - Recolte
    QUEST_NPC_SECONDAIRE_RECOLTE_N1("Quest_Npc_Secondaire_Recolte_N1", "Flora l'Herboriste", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N2("Quest_Npc_Secondaire_Recolte_N2", "Gavin le Fermier", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N3("Quest_Npc_Secondaire_Recolte_N3", "Rosalie la Fleuriste", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N4("Quest_Npc_Secondaire_Recolte_N4", "Cedric le Bucheron", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N5("Quest_Npc_Secondaire_Recolte_N5", "Willow la Druide", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N6("Quest_Npc_Secondaire_Recolte_N6", "Jasper le Cueilleur", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N7("Quest_Npc_Secondaire_Recolte_N7", "Hazel la Sorciere", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N8("Quest_Npc_Secondaire_Recolte_N8", "Alden le Vigneron", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N9("Quest_Npc_Secondaire_Recolte_N9", "Fern la Botaniste", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N10("Quest_Npc_Secondaire_Recolte_N10", "Moss le Sylvain", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N11("Quest_Npc_Secondaire_Recolte_N11", "Primrose l'Apothicaire", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N12("Quest_Npc_Secondaire_Recolte_N12", "Reed le Bucheron", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N13("Quest_Npc_Secondaire_Recolte_N13", "Coral la Plongeuse", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N14("Quest_Npc_Secondaire_Recolte_N14", "Bramble le Garde-Foret", true),
    QUEST_NPC_SECONDAIRE_RECOLTE_N15("Quest_Npc_Secondaire_Recolte_N15", "Sage l'Ermite", true),

    // PNJ de quetes secondaires - Collection
    QUEST_NPC_SECONDAIRE_COLLECTION_N1("Quest_Npc_Secondaire_Collection_N1", "Aldric le Marchand", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N2("Quest_Npc_Secondaire_Collection_N2", "Elyna la Pretresse", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N3("Quest_Npc_Secondaire_Collection_N3", "Grunk le Parieur", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N4("Quest_Npc_Secondaire_Collection_N4", "Theomund le Banquier", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N5("Quest_Npc_Secondaire_Collection_N5", "Capitaine Varn", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N6("Quest_Npc_Secondaire_Collection_N6", "Dame Isolde", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N7("Quest_Npc_Secondaire_Collection_N7", "Gaston le Percepteur", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N8("Quest_Npc_Secondaire_Collection_N8", "Silas l'Informateur", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N9("Quest_Npc_Secondaire_Collection_N9", "Bromdar l'Architecte", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N10("Quest_Npc_Secondaire_Collection_N10", "Emissaire Renard", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N11("Quest_Npc_Secondaire_Collection_N11", "Maximilien le Collectionneur", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N12("Quest_Npc_Secondaire_Collection_N12", "General Kael", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N13("Quest_Npc_Secondaire_Collection_N13", "Aldwin le Tresorier", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N14("Quest_Npc_Secondaire_Collection_N14", "Maitresse Vivianne", true),
    QUEST_NPC_SECONDAIRE_COLLECTION_N15("Quest_Npc_Secondaire_Collection_N15", "Sage Tharion", true),

    // PNJ de quetes secondaires - Exploration
    QUEST_NPC_SECONDAIRE_EXPLORATION_N1("Quest_Npc_Secondaire_Exploration_N1", "Aldric le Cartographe", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N2("Quest_Npc_Secondaire_Exploration_N2", "Elise la Vagabonde", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N3("Quest_Npc_Secondaire_Exploration_N3", "Barnabe le Prospecteur", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N4("Quest_Npc_Secondaire_Exploration_N4", "Professeure Miriel", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N5("Quest_Npc_Secondaire_Exploration_N5", "Gaspard le Chineur", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N6("Quest_Npc_Secondaire_Exploration_N6", "Torven le Deserteur", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N7("Quest_Npc_Secondaire_Exploration_N7", "Rohan le Speleologue", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N8("Quest_Npc_Secondaire_Exploration_N8", "Dame Heloise", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N9("Quest_Npc_Secondaire_Exploration_N9", "Odilon le Gardien", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N10("Quest_Npc_Secondaire_Exploration_N10", "Felix le Repenti", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N11("Quest_Npc_Secondaire_Exploration_N11", "Druide Thalion", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N12("Quest_Npc_Secondaire_Exploration_N12", "Capitaine Morven", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N13("Quest_Npc_Secondaire_Exploration_N13", "Amiral Veyran", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N14("Quest_Npc_Secondaire_Exploration_N14", "Maitre Kael", true),
    QUEST_NPC_SECONDAIRE_EXPLORATION_N15("Quest_Npc_Secondaire_Exploration_N15", "Conservateur Lysandre", true),

    // PNJ de quetes secondaires - Duel
    QUEST_NPC_SECONDAIRE_DUEL_N1("Quest_Npc_Secondaire_Duel_N1", "Vieux Garreth", true),
    QUEST_NPC_SECONDAIRE_DUEL_N2("Quest_Npc_Secondaire_Duel_N2", "Gladiatrice Vala", true),
    QUEST_NPC_SECONDAIRE_DUEL_N3("Quest_Npc_Secondaire_Duel_N3", "Marchand Lucien", true),
    QUEST_NPC_SECONDAIRE_DUEL_N4("Quest_Npc_Secondaire_Duel_N4", "Chevalier Banni Aldric", true),
    QUEST_NPC_SECONDAIRE_DUEL_N5("Quest_Npc_Secondaire_Duel_N5", "Heraut du Tournoi", true),
    QUEST_NPC_SECONDAIRE_DUEL_N6("Quest_Npc_Secondaire_Duel_N6", "Maitre Hiro", true),
    QUEST_NPC_SECONDAIRE_DUEL_N7("Quest_Npc_Secondaire_Duel_N7", "Colosse Bjorn", true),
    QUEST_NPC_SECONDAIRE_DUEL_N8("Quest_Npc_Secondaire_Duel_N8", "Forgeronne Elna", true),
    QUEST_NPC_SECONDAIRE_DUEL_N9("Quest_Npc_Secondaire_Duel_N9", "L'Ombre de l'Arene", true),
    QUEST_NPC_SECONDAIRE_DUEL_N10("Quest_Npc_Secondaire_Duel_N10", "Gouverneur Valcrest", true),
    QUEST_NPC_SECONDAIRE_DUEL_N11("Quest_Npc_Secondaire_Duel_N11", "Ancien Champion Rodrik", true),
    QUEST_NPC_SECONDAIRE_DUEL_N12("Quest_Npc_Secondaire_Duel_N12", "Capitaine Mercenaire", true),
    QUEST_NPC_SECONDAIRE_DUEL_N13("Quest_Npc_Secondaire_Duel_N13", "Heritiere Selene", true),
    QUEST_NPC_SECONDAIRE_DUEL_N14("Quest_Npc_Secondaire_Duel_N14", "Gladiateur Korthak", true),
    QUEST_NPC_SECONDAIRE_DUEL_N15("Quest_Npc_Secondaire_Duel_N15", "Chroniqueur Aldwin", true),
    QUEST_NPC_SECONDAIRE_DUEL_N16("Quest_Npc_Secondaire_Duel_N16", "Maitre d'Armes Trahi", true),
    QUEST_NPC_SECONDAIRE_DUEL_N17("Quest_Npc_Secondaire_Duel_N17", "Dame de Confrerie", true),
    QUEST_NPC_SECONDAIRE_DUEL_N18("Quest_Npc_Secondaire_Duel_N18", "Forgeron Legendaire", true),
    QUEST_NPC_SECONDAIRE_DUEL_N19("Quest_Npc_Secondaire_Duel_N19", "Mourant Legendaire", true),
    QUEST_NPC_SECONDAIRE_DUEL_N20("Quest_Npc_Secondaire_Duel_N20", "Spectre Royal", true);

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