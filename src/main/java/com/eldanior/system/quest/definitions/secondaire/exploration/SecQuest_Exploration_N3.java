package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N3 : L'Heritage du Prospecteur.
 * Un prospecteur a la retraite veut que quelqu'un retrouve ses anciennes caches.
 * Difficulte E - 5 coffres a decouvrir.
 */
public class SecQuest_Exploration_N3 extends NpcDialogueQuest {

    public SecQuest_Exploration_N3() {
        super(
                "sec_exploration_3",
                "L'Heritage du Prospecteur",
                "Un prospecteur a la retraite vous confie la mission de retrouver ses caches perdues.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                null, 5,
                500, 1200, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N3",

                List.of(
                    new DialoguePage("Barnabe le Prospecteur",
                            "Approche, approche... Tu vois ces mains ? " +
                            "Elles ont creuse la terre d'Eldanior pendant quarante ans.\n\n" +
                            "J'ai cache des coffres un peu partout dans ma jeunesse. " +
                            "Des reserves, des outils, des provisions... " +
                            "Mais ma memoire n'est plus ce qu'elle etait.",
                            "Un homme robuste aux mains calleuses"),

                    new DialoguePage("Barnabe le Prospecteur",
                            "J'aimerais que quelqu'un retrouve au moins 5 de mes anciennes " +
                            "caches. Ce n'est pas tant pour le contenu — c'est pour savoir " +
                            "qu'elles existent encore.\n\n" +
                            "Chaque coffre que tu trouveras, c'est un morceau de ma vie " +
                            "que tu fais revivre.",
                            "Il regarde au loin avec nostalgie"),

                    new DialoguePage("Barnabe le Prospecteur",
                            "Les coffres sont souvent pres des points d'eau, des grottes, " +
                            "ou des formations rocheuses. C'est la que je m'arretais " +
                            "pour me reposer autrefois.\n\n" +
                            "Bonne chance, jeune explorateur. Et prends soin de toi la-bas.",
                            "Il vous serre la main fermement")
                ),

                null,
                null,
                1440
        );
    }
}
