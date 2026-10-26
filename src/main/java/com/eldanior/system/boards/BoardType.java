package com.eldanior.system.boards;

import java.util.List;

/**
 * Tableaux poses en ville par un admin (blocs-objets sans recette ni vente) : touche d'interaction -> BoardScreen. MAITRE :
 * meme fenetre, ouverte par le Maitre de guilde (PNJ).
 * Modeles vanilla : parchemin cloue au mur du tableau de prime d'origine (Objective_Bounty, Map.blockymodel), banniere des
 * ruines humaines pour la guilde. L'identifiant de bloc est celui de l'objet.
 */
public enum BoardType {
    PRIME("Eldanior_Tableau_Prime", "Tableau de prime",
            "Avis de recherche du royaume. Chaque criminel a un prix : à vous de le toucher.",
            List.of("Recherchés", "Contrats")),
    GUILDE("Eldanior_Tableau_Guilde", "Tableau de guilde",
            "La gloire d'une guilde se mesure à ses exploits. Que la vôtre y figure.",
            List.of("Classement", "Contrats", "Missions")),
    /** Fenetre du Maitre de guilde (PNJ, pas un bloc). */
    MAITRE(null, "Maître de guilde",
            "Une guilde se fonde sur l'or et se grandit par l'effort. Que puis-je pour vous ?",
            List.of("Fonder", "Améliorations"));

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
