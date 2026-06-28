package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N14 - Le Pacte des Guildes
 * La maitresse des guildes marchandes demande une contribution colossale.
 * Difficulte S - 200000 Or a collecter.
 */
public class SecQuest_Collection_N14 extends NpcDialogueQuest {

    public SecQuest_Collection_N14() {
        super(
                "sec_collection_14",
                "Le Pacte des Guildes",
                "Maitresse Vivianne propose un siege au Conseil des Guildes Marchandes.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                null, 200000,
                30000, 100000, null,
                null,
                "Quest_Npc_Secondaire_Collection_N14",

                List.of(
                    new DialoguePage("Maitresse Vivianne",
                            "Aventurier. Je ne perds pas mon temps avec les petits joueurs, " +
                            "alors soyons directs.\n\n" +
                            "Je suis Maitresse Vivianne, presidente du Conseil " +
                            "des Guildes Marchandes d'Eldanior. Tout le commerce " +
                            "passe par nous. Tout.",
                            "Une femme imposante dans une salle de reunion luxueuse"),

                    new DialoguePage("Maitresse Vivianne",
                            "Un siege au Conseil vient de se liberer. " +
                            "Le dernier occupant a eu un... accident malheureux. " +
                            "Ne posez pas de questions.\n\n" +
                            "Ce siege donne acces aux meilleures routes commerciales, " +
                            "aux contrats les plus lucratifs, et a une influence politique " +
                            "considerable.",
                            "Elle tapote la table avec ses ongles parfaitement manucures"),

                    new DialoguePage("Maitresse Vivianne",
                            "Le prix d'entree ? 200000 pieces d'or. Non negociable.\n\n" +
                            "C'est le cout pour prouver que vous etes a la hauteur " +
                            "des enjeux. Les faibles et les indecis n'ont pas " +
                            "leur place a cette table.\n\n" +
                            "Revenez avec l'or, et le Conseil vous accueillera. " +
                            "Echouez, et ne revenez pas du tout.",
                            "Elle vous congedie d'un geste elegant mais ferme")
                ),

                null,
                null,
                1440
        );
    }
}
