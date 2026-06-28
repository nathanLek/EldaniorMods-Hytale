package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N8 : La Route des Marchands.
 * Une ancienne marchande raconte les coffres perdus le long des routes commerciales.
 * Difficulte C - 15 coffres a decouvrir.
 */
public class SecQuest_Exploration_N8 extends NpcDialogueQuest {

    public SecQuest_Exploration_N8() {
        super(
                "sec_exploration_8",
                "La Route des Marchands",
                "Une ancienne marchande vous envoie recuperer des coffres perdus sur les routes commerciales.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                null, 15,
                2200, 6000, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N8",

                List.of(
                    new DialoguePage("Dame Heloise",
                            "Dans ma jeunesse, les routes d'Eldanior etaient prosperes. " +
                            "Les caravanes de marchands transportaient des tresors " +
                            "d'une cite a l'autre.\n\n" +
                            "Puis les bandits sont arrives. Et les monstres. " +
                            "Beaucoup de marchands ont abandonne leurs marchandises " +
                            "pour sauver leur vie.",
                            "Une femme elegante d'un certain age"),

                    new DialoguePage("Dame Heloise",
                            "Les coffres sont restes la ou ils ont ete abandonnes. " +
                            "Sur les bords des routes, dans les ruines des relais, " +
                            "dans les fosses et les ravins.\n\n" +
                            "Personne n'a pris la peine de les recuperer. " +
                            "Trop dangereux, disent-ils.",
                            "Elle soupire en regardant un vieux registre commercial"),

                    new DialoguePage("Dame Heloise",
                            "Retrouvez 15 de ces coffres. Le contenu vous appartient, " +
                            "bien entendu. Je n'en ai plus besoin.\n\n" +
                            "Ce que je veux, c'est savoir que quelqu'un se souvient " +
                            "de cette epoque. Que les routes marchandes n'ont pas " +
                            "ete oubliees pour toujours.",
                            "Elle vous remet un ancien itineraire de caravane")
                ),

                null,
                null,
                1440
        );
    }
}
