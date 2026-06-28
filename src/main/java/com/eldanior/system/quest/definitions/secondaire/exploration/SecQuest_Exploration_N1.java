package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N1 : Les Coffres Oublies.
 * Un vieux cartographe demande au joueur de retrouver des coffres perdus.
 * Difficulte F - 3 coffres a decouvrir.
 */
public class SecQuest_Exploration_N1 extends NpcDialogueQuest {

    public SecQuest_Exploration_N1() {
        super(
                "sec_exploration_1",
                "Les Coffres Oublies",
                "Un vieux cartographe vous demande de retrouver des coffres perdus dans les environs.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.F,
                null, 3,
                200, 500, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N1",

                List.of(
                    new DialoguePage("Aldric le Cartographe",
                            "Ah, un voyageur ! Parfait, j'ai besoin d'aide.\n\n" +
                            "Je suis Aldric, cartographe de la guilde des Explorateurs. " +
                            "Depuis des annees, je cartographie chaque recoin d'Eldanior, " +
                            "mais mes vieilles jambes ne me portent plus aussi loin qu'avant.",
                            "Un vieil homme entoure de cartes poussiereuses"),

                    new DialoguePage("Aldric le Cartographe",
                            "J'ai repere sur mes anciennes cartes l'emplacement de coffres " +
                            "abandonnes dans la region. Des voyageurs les ont laisses la " +
                            "il y a des lustres, et personne ne s'en souvient.\n\n" +
                            "Si vous pouviez en retrouver 3, je vous recompenserai grassement.",
                            "Il pointe du doigt des croix sur une carte jaunie"),

                    new DialoguePage("Aldric le Cartographe",
                            "Explorez les alentours, ouvrez l'oeil ! " +
                            "Les coffres peuvent etre caches n'importe ou : dans des grottes, " +
                            "derriere des arbres, au sommet de collines...\n\n" +
                            "Revenez me voir quand vous en aurez trouve 3.",
                            "Il vous tend une vieille boussole cabossee")
                ),

                null,
                null,
                1440
        );
    }
}
