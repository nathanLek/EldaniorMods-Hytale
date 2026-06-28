package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N13 : L'Atlas Perdu.
 * Un navigateur legendaire a perdu son atlas et demande au joueur de cartographier la region.
 * Difficulte S - 25 coffres a decouvrir.
 */
public class SecQuest_Exploration_N13 extends NpcDialogueQuest {

    public SecQuest_Exploration_N13() {
        super(
                "sec_exploration_13",
                "L'Atlas Perdu",
                "Un navigateur legendaire vous confie la mission de cartographier les coffres de toute la region.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                null, 25,
                6000, 25000, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N13",

                List.of(
                    new DialoguePage("Amiral Veyran",
                            "On m'appelle l'Amiral Veyran. J'ai navigue sur toutes " +
                            "les mers connues et cartographie des terres que personne " +
                            "n'avait jamais vues.\n\n" +
                            "Mais mon plus grand ouvrage — l'Atlas d'Eldanior — " +
                            "a ete perdu lors d'un naufrage. " +
                            "Toute une vie de travail, engloutie.",
                            "Un homme imposant en uniforme de marine use"),

                    new DialoguePage("Amiral Veyran",
                            "Pour reconstituer l'Atlas, j'ai besoin de donnees fraiches. " +
                            "Chaque coffre que vous decouvrirez est un point de reference " +
                            "sur ma carte.\n\n" +
                            "Plus vous en trouvez, plus l'Atlas sera complet. " +
                            "Et croyez-moi, un Atlas complet d'Eldanior... " +
                            "ca vaut une fortune.",
                            "Il deroule une carte a moitie vierge"),

                    new DialoguePage("Amiral Veyran",
                            "J'ai besoin de 25 points de reference. 25 coffres decouverts. " +
                            "C'est une expedition d'envergure, et seul un explorateur " +
                            "d'exception peut y arriver.\n\n" +
                            "La recompense sera a la hauteur de l'exploit. " +
                            "Vous avez ma parole d'amiral.",
                            "Il vous salue d'un geste militaire")
                ),

                null,
                null,
                1440
        );
    }
}
