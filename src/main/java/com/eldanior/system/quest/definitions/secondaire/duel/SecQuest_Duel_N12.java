package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N12 : Le Contrat du Mercenaire
 * Difficulte B - Gagner 10 duels
 */
public class SecQuest_Duel_N12 extends NpcDialogueQuest {

    public SecQuest_Duel_N12() {
        super(
                "sec_duel_12",
                "Le Contrat du Mercenaire",
                "Un capitaine mercenaire recrute des guerriers d'elite pour sa compagnie.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                null, 10,
                2500, 10000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N12",

                List.of(
                    new DialoguePage("Capitaine Voss",
                            "La Compagnie du Loup Noir ne prend pas n'importe qui. " +
                            "On est les meilleurs mercenaires du continent, et ca se merite.\n\n" +
                            "J'ai vu ton nom circuler dans les arenes. " +
                            "Pas mal, mais pas suffisant pour nous.",
                            "Un homme balafre en armure de cuir noir, entoure de gardes"),

                    new DialoguePage("Capitaine Voss",
                            "Voici le test d'admission : dix victoires en duel. " +
                            "Contre des adversaires serieux, pas des fermiers armes de fourches.\n\n" +
                            "Mes hommes observeront tes combats. On veut voir de la technique, " +
                            "de l'adaptabilite, et surtout du cran.",
                            "Il croise les bras et vous jauge du regard"),

                    new DialoguePage("Capitaine Voss",
                            "La paye est excellente, les missions sont dangereuses, " +
                            "et la fraternite est reelle. C'est ca, le Loup Noir.\n\n" +
                            "Dix victoires. Pas de seconde chance.",
                            "Il tourne les talons et rejoint ses hommes")
                ),
                null, null, 1440
        );
    }
}
