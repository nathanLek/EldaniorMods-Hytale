package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N14 : La Benediction de l'Eclaireur.
 * Un maitre eclaireur met a l'epreuve le joueur pour qu'il devienne un explorateur reconnu.
 * Difficulte S - 30 coffres a decouvrir.
 */
public class SecQuest_Exploration_N14 extends NpcDialogueQuest {

    public SecQuest_Exploration_N14() {
        super(
                "sec_exploration_14",
                "La Benediction de l'Eclaireur",
                "Un maitre eclaireur vous met a l'epreuve : prouvez votre valeur d'explorateur.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                null, 30,
                7500, 30000, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N14",

                List.of(
                    new DialoguePage("Maitre Kael",
                            "Halte, voyageur. Tu es devant le campement des Eclaireurs " +
                            "d'Eldanior. Nous sommes une confrerie ancienne, " +
                            "gardienne des chemins et des secrets de cette terre.\n\n" +
                            "Pour nous rejoindre, il faut passer une epreuve. " +
                            "Une epreuve que peu reussissent.",
                            "Un homme austere en armure legere, arc dans le dos"),

                    new DialoguePage("Maitre Kael",
                            "L'epreuve est simple a comprendre, mais redoutable a accomplir. " +
                            "Tu dois decouvrir 30 coffres dissemines a travers Eldanior.\n\n" +
                            "Pas 10, pas 20. Trente. Chacun est une preuve que tu connais " +
                            "cette terre aussi bien que nous.",
                            "Il croise les bras, impassible"),

                    new DialoguePage("Maitre Kael",
                            "Si tu reussis, tu recevras la Benediction de l'Eclaireur. " +
                            "Un honneur rare, accorde a ceux qui ont prouve leur maitrise " +
                            "de l'exploration.\n\n" +
                            "La recompense sera genereuse. " +
                            "Mais surtout, tu auras gagne notre respect. " +
                            "Et ca, ca n'a pas de prix.",
                            "Il vous tend une echarpe de reconnaissance")
                ),

                null,
                null,
                1440
        );
    }
}
