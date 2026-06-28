package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N10 - La Dot de la Princesse
 * Un emissaire royal cherche quelqu'un pour reunir une dot diplomatique.
 * Difficulte B - 40000 Or a collecter.
 */
public class SecQuest_Collection_N10 extends NpcDialogueQuest {

    public SecQuest_Collection_N10() {
        super(
                "sec_collection_10",
                "La Dot de la Princesse",
                "L'emissaire Renard cherche un bienfaiteur pour la dot diplomatique.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                null, 40000,
                7000, 18000, null,
                null,
                "Quest_Npc_Secondaire_Collection_N10",

                List.of(
                    new DialoguePage("Emissaire Renard",
                            "Aventurier, un instant je vous prie. Je suis l'Emissaire Renard, " +
                            "au service de Sa Majeste.\n\n" +
                            "La situation est delicate. La princesse Aelindra " +
                            "doit epouser le prince du royaume voisin pour sceller " +
                            "une alliance cruciale. Mais la dot...",
                            "Un homme en tenue diplomatique, visiblement stresse"),

                    new DialoguePage("Emissaire Renard",
                            "Les caisses du royaume sont vides apres la derniere guerre. " +
                            "Si nous ne reunissons pas la dot a temps, " +
                            "l'alliance tombe a l'eau. Et sans cette alliance, " +
                            "nous serons vulnerables a une invasion.\n\n" +
                            "C'est toute la geopolitique d'Eldanior qui est en jeu.",
                            "Il montre une carte avec des mouvements de troupes ennemies"),

                    new DialoguePage("Emissaire Renard",
                            "La dot s'eleve a 40000 pieces d'or. " +
                            "Je sais, c'est considerable.\n\n" +
                            "Mais pensez-y : celui qui sauve un mariage royal " +
                            "entre dans les livres d'histoire. " +
                            "Et la couronne sait recompenser ses allies.\n\n" +
                            "Pouvez-vous nous aider ?",
                            "Il s'incline respectueusement")
                ),

                null,
                null,
                1440
        );
    }
}
