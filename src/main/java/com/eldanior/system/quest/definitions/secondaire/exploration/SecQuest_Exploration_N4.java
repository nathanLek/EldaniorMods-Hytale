package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N4 : Cartographie des Ruines.
 * Une archeoloque engage le joueur pour explorer des ruines et trouver des coffres anciens.
 * Difficulte E - 8 coffres a decouvrir.
 */
public class SecQuest_Exploration_N4 extends NpcDialogueQuest {

    public SecQuest_Exploration_N4() {
        super(
                "sec_exploration_4",
                "Cartographie des Ruines",
                "Une archeologue vous engage pour explorer des ruines et localiser des coffres anciens.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                null, 8,
                700, 1800, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N4",

                List.of(
                    new DialoguePage("Professeure Miriel",
                            "Bonjour ! Je suis Miriel, archeologue de l'Academie d'Eldanior. " +
                            "Mes recherches m'ont menee a une decouverte fascinante.\n\n" +
                            "Les ruines disseminees dans la region contiennent des coffres " +
                            "datant de l'ancien empire. Leur contenu pourrait nous en apprendre " +
                            "beaucoup sur nos ancetres.",
                            "Une femme en tenue de fouille, des outils a la ceinture"),

                    new DialoguePage("Professeure Miriel",
                            "Le probleme, c'est que ces ruines sont dangereuses. " +
                            "Des creatures y ont elu domicile, et les pieges anciens " +
                            "sont encore actifs par endroits.\n\n" +
                            "J'ai besoin de quelqu'un de courageux pour y penetrer " +
                            "et localiser au moins 8 coffres.",
                            "Elle vous montre des croquis de ruines"),

                    new DialoguePage("Professeure Miriel",
                            "Chaque coffre que vous trouverez est une piece du puzzle " +
                            "de notre histoire. Ne les detruisez surtout pas !\n\n" +
                            "Ouvrez-les, notez leur contenu, et revenez me faire " +
                            "votre rapport. La science vous sera reconnaissante !",
                            "Elle note quelque chose dans son carnet")
                ),

                null,
                null,
                1440
        );
    }
}
