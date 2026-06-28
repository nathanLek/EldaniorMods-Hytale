package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N2 : Tresors des Sentiers.
 * Une aventuriere curieuse veut prouver que les sentiers caches regorgent de tresors.
 * Difficulte F - 5 coffres a decouvrir.
 */
public class SecQuest_Exploration_N2 extends NpcDialogueQuest {

    public SecQuest_Exploration_N2() {
        super(
                "sec_exploration_2",
                "Tresors des Sentiers",
                "Une aventuriere vous met au defi de trouver des coffres le long des sentiers oublies.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.F,
                null, 5,
                350, 800, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N2",

                List.of(
                    new DialoguePage("Elise la Vagabonde",
                            "He, toi ! Tu as l'air de quelqu'un qui aime se perdre. " +
                            "Ca tombe bien, moi aussi !\n\n" +
                            "Je suis Elise. Je parcours les chemins d'Eldanior depuis " +
                            "que je suis gamine. Et tu sais quoi ? Les sentiers oublies " +
                            "sont pleins de surprises.",
                            "Une jeune femme avec un sac a dos use"),

                    new DialoguePage("Elise la Vagabonde",
                            "Les anciens voyageurs laissaient des coffres le long " +
                            "des routes pour les generations futures. La plupart des gens " +
                            "les ignorent, trop presses d'arriver a destination.\n\n" +
                            "Pas moi. Et pas toi non plus, j'espere.",
                            "Elle montre un sentier qui s'enfonce dans la foret"),

                    new DialoguePage("Elise la Vagabonde",
                            "Trouve 5 coffres et reviens m'en parler. " +
                            "Je veux savoir ce que tu as decouvert !\n\n" +
                            "Et un conseil : ne reste pas sur les grands chemins. " +
                            "Les vrais tresors se cachent toujours hors des sentiers battus.",
                            "Elle ajuste son chapeau et sourit")
                ),

                null,
                null,
                1440
        );
    }
}
