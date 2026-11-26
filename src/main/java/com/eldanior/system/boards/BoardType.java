package com.eldanior.system.boards;

import java.util.List;

/**
 * Tableaux poses en ville par un admin (blocs-objets sans recette ni vente) : touche d'interaction -> BoardScreen. MAITRE :
 * meme fenetre, ouverte par le Maitre de guilde (PNJ). QUETES : quetes journalieres (QuestBoardScreen) ; CLASSEMENT :
 * classements du royaume (RankingBoardScreen) ; DUEL : duels en cours, champions, historique (DuelBoardScreen).
 * Modeles vanilla : parchemin cloue au mur du tableau de prime d'origine (Objective_Bounty, Map.blockymodel), banniere des
 * ruines humaines pour la guilde. L'identifiant de bloc est celui de l'objet.
 */
public enum BoardType {
    PRIME("Eldanior_Tableau_Prime", "Tableau de prime",
            "Avis de recherche du royaume. Chaque criminel a un prix : à vous de le toucher.",
            List.of("Recherchés", "Contrats")),
    /** 2026-11-19 : s'ouvre en cartes (QuestChaptersPage, GuildQuests) ; le classement des guildes est au tableau des classements. */
    GUILDE("Eldanior_Tableau_Guilde", "Tableau de guilde",
            "La gloire d'une guilde se mesure à ses exploits. Que la vôtre y figure.",
            List.of("Quêtes de guilde", "Missions de guilde")),
    QUETES("Eldanior_Tableau_Quetes", "Tableau des quêtes",
            "Chaque jour, le royaume a besoin de bras. Prenez une affiche, revenez avec la preuve.",
            List.of("Du jour", "En cours", "À réclamer")),
    CLASSEMENT("Eldanior_Tableau_Classement", "Tableau des classements",
            "Les noms gravés ici sont ceux dont on parle dans les tavernes.",
            List.of("Chasseurs", "Combattants", "Guildes", "Familles")),   // Guildes : ex-classement du tableau de guilde (2026-11-19)
    DUEL("Eldanior_Tableau_Duel", "Tableau des duels",
            "Un duel se gagne à 1 point de vie près. Le perdant laisse un dixième de son expérience.",
            List.of("En cours", "Champions", "Mes duels")),
    /** Fenetre du Maitre de guilde (PNJ, pas un bloc). */
    MAITRE(null, "Maître de guilde",
            "Une guilde se fonde sur l'or et se grandit par l'effort. Que puis-je pour vous ?",
            List.of("Fonder", "Améliorations")),
    /** Fenetre de l'intendant d'une famille (ancienne fenetre ; remplacee par guards.IntendantPage le 2026-11-18) : gestion de la maison. */
    INTENDANT(null, "Intendant",
            "Une maison tient par ses registres autant que par ses lames. Que dois-je consigner ?",
            List.of("Listes", "Ordres", "Gestion", "Domaines", "Biens", "Achats")),
    /**
     * Bureau des terres (2026-11-13, guards.LandOfficeScreen) : Commercant Immobilier (biens du joueur, marche, terres) et
     * intendant pour les Ducs et Comtes (achat d'un Territoire ou d'une ville de ses domaines).
     */
    IMMOBILIER(null, "Bureau des terres",
            "Une maison, un champ, un duché : tout s'achète, pourvu que le titre suive la bourse.",
            List.of("Mes biens", "Marché", "Terres"));

    private final String blockId, label, quote;
    private final List<String> tabs;

    BoardType(String blockId, String label, String quote, List<String> tabs) {
        this.blockId = blockId; this.label = label; this.quote = quote; this.tabs = tabs;
    }

    public String getBlockId() { return blockId; }
    public String getLabel() { return label; }
    /** Replique affichee sous le titre. */
    public String getQuote() { return quote; }
    /** Onglets de la fenetre (au plus BoardScreen.TABS). */
    public List<String> getTabs() { return tabs; }

    /** Tableau d'apres l'identifiant de bloc ou d'objet (variantes de rotation comprises), null sinon. */
    public static BoardType of(String id) {
        if (id == null) return null;
        for (BoardType t : values()) if (t.blockId != null && id.startsWith(t.blockId)) return t;
        return null;
    }
}
