package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N17 : Le Defi des Cent Lames
 * Difficulte A - Gagner 20 duels
 */
public class SecQuest_Duel_N17 extends NpcDialogueQuest {

    public SecQuest_Duel_N17() {
        super(
                "sec_duel_17",
                "Le Defi des Cent Lames",
                "Une societe secrete de duellistes propose un defi redoutable.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                null, 20,
                5500, 28000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N17",

                List.of(
                    new DialoguePage("Dame Selene",
                            "Les Cent Lames. Vous en avez entendu parler, n'est-ce pas ? " +
                            "Nous sommes une confrerie de duellistes d'elite.\n\n" +
                            "Nos membres sont des rois, des generaux, des assassins. " +
                            "Des gens que le monde craint et respecte.",
                            "Une femme elegante en robe de combat noire"),

                    new DialoguePage("Dame Selene",
                            "Notre epreuve d'initiation est simple dans son concept, " +
                            "brutale dans son execution : vingt victoires en duel.\n\n" +
                            "Pas contre des novices. Contre des adversaires dignes. " +
                            "Nous serons la pour observer et juger chaque combat.",
                            "Elle fait tournoyer une rapiere avec une precision chirurgicale"),

                    new DialoguePage("Dame Selene",
                            "Si vous reussissez, les portes de notre confrerie s'ouvriront. " +
                            "Vous aurez acces a nos techniques, nos contacts, notre influence.\n\n" +
                            "Si vous echouez... eh bien, personne ne se souvient des perdants.",
                            "Elle range sa rapiere et disparait dans la foule")
                ),
                null, null, 1440
        );
    }
}
