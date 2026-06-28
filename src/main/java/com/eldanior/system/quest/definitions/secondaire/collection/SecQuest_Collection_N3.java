package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N3 - Le Pari du Troll
 * Un troll parieur met au defi le joueur d'amasser de l'or.
 * Difficulte E - 2000 Or a collecter.
 */
public class SecQuest_Collection_N3 extends NpcDialogueQuest {

    public SecQuest_Collection_N3() {
        super(
                "sec_collection_3",
                "Le Pari du Troll",
                "Grunk le troll parieur vous lance un defi financier.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                null, 2000,
                600, 1000, null,
                null,
                "Quest_Npc_Secondaire_Collection_N3",

                List.of(
                    new DialoguePage("Grunk",
                            "Hehe ! Petit humain venir voir Grunk ?\n\n" +
                            "Grunk connaitre tout le monde ici. Grunk savoir qui est riche, " +
                            "qui est pauvre. Et toi... toi sentir la pauvrete !",
                            "Un troll massif assis sur un tas de pieces"),

                    new DialoguePage("Grunk",
                            "Grunk proposer un pari. Toi reussir a avoir 2000 pieces d'or, " +
                            "et Grunk te donner quelque chose de special.\n\n" +
                            "Toi echouer ? Grunk rire tres fort. TRES fort. " +
                            "Tout le village entendre Grunk rire de toi.",
                            "Il croise ses bras enormes avec un sourire moqueur"),

                    new DialoguePage("Grunk",
                            "Alors ? Petit humain accepter le pari de Grunk ?\n\n" +
                            "Pas besoin donner l'or a Grunk, hein ! Juste montrer que toi capable. " +
                            "Grunk respecter ceux qui savent faire fortune.\n\n" +
                            "Allez, va chercher tes pieces !",
                            "Il vous pousse gentiment vers la sortie")
                ),

                null,
                null,
                1440
        );
    }
}
