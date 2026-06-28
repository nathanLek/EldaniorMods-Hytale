package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N3 : Le Pari du Marchand
 * Difficulte E - Gagner 3 duels
 */
public class SecQuest_Duel_N3 extends NpcDialogueQuest {

    public SecQuest_Duel_N3() {
        super(
                "sec_duel_3",
                "Le Pari du Marchand",
                "Un marchand parieur vous offre une belle somme si vous gagnez des duels.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                null, 3,
                500, 2000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N3",

                List.of(
                    new DialoguePage("Tobias le Parieur",
                            "Psst ! Toi, l'aventurier ! Tu as l'air d'avoir les epaules pour le combat.\n\n" +
                            "Je suis Tobias, marchand de profession, parieur par passion. " +
                            "Et j'ai un flair infaillible pour reperer les gagnants.",
                            "Un homme bedonnant avec des bagues a chaque doigt"),

                    new DialoguePage("Tobias le Parieur",
                            "Voici le marche : je parie sur toi. Tu gagnes trois duels, " +
                            "et je te file une part de mes gains. Tout le monde y gagne !\n\n" +
                            "Enfin, sauf tes adversaires, evidemment.",
                            "Il sort une bourse de pieces d'or"),

                    new DialoguePage("Tobias le Parieur",
                            "Alors, on tope la ? Trois victoires et tu seras bien plus riche " +
                            "qu'en chassant des lapins dans la foret.\n\n" +
                            "Ne me deçois pas, gamin. Ma reputation est en jeu !",
                            "Il tend une main potelue pour sceller l'accord")
                ),
                null, null, 1440
        );
    }
}
