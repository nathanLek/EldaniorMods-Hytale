package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N16 : La Vengeance du Maitre d'Armes
 * Difficulte A - Gagner 20 duels
 */
public class SecQuest_Duel_N16 extends NpcDialogueQuest {

    public SecQuest_Duel_N16() {
        super(
                "sec_duel_16",
                "La Vengeance du Maitre d'Armes",
                "Un maitre d'armes trahi cherche quelqu'un pour humilier son ancien eleve.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                null, 20,
                5000, 25000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N16",

                List.of(
                    new DialoguePage("Maitre Corvain",
                            "J'ai consacre vingt ans a former des combattants d'elite. " +
                            "Mon meilleur eleve, Theron, m'a tout vole. " +
                            "Mes techniques, mes eleves, ma reputation.\n\n" +
                            "Aujourd'hui il se pavane comme le plus grand duelliste du royaume. " +
                            "Ca me rend malade.",
                            "Un homme sec au regard d'acier, couvert de vieilles blessures"),

                    new DialoguePage("Maitre Corvain",
                            "Je suis trop vieux pour l'affronter moi-meme. " +
                            "Mais je peux former quelqu'un qui le surpassera.\n\n" +
                            "Vingt duels gagnes. C'est le nombre qu'il faut pour maitriser " +
                            "mes techniques les plus avancees. Des mouvements qu'il n'a jamais appris " +
                            "parce qu'il est parti trop tot.",
                            "Il sort une lame d'entrainement d'un coffre poussiereux"),

                    new DialoguePage("Maitre Corvain",
                            "Chaque victoire t'apprendra quelque chose de nouveau. " +
                            "Quand tu en auras vingt, tu seras pret a tout affronter.\n\n" +
                            "Et peut-etre qu'un jour, tu croiseras Theron sur ton chemin. " +
                            "Ce jour-la, souviens-toi de qui t'a forme.",
                            "Un sourire amer se dessine sur ses levres")
                ),
                null, null, 1440
        );
    }
}
