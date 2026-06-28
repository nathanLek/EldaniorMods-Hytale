package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N7 : Expedition Souterraine.
 * Un speologue passione veut que le joueur explore les grottes d'Eldanior.
 * Difficulte C - 12 coffres a decouvrir.
 */
public class SecQuest_Exploration_N7 extends NpcDialogueQuest {

    public SecQuest_Exploration_N7() {
        super(
                "sec_exploration_7",
                "Expedition Souterraine",
                "Un speleologue vous propose d'explorer les grottes et cavernes d'Eldanior.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                null, 12,
                1800, 5000, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N7",

                List.of(
                    new DialoguePage("Rohan le Speleologue",
                            "Les profondeurs d'Eldanior sont un monde a part entiere. " +
                            "Peu de gens osent s'y aventurer, et c'est bien dommage.\n\n" +
                            "Je suis Rohan, et les grottes sont ma passion. " +
                            "J'ai passe ma vie a ramper dans des tunnels, " +
                            "a nager dans des lacs souterrains, a decouvrir des merveilles.",
                            "Un homme couvert de boue avec une lanterne"),

                    new DialoguePage("Rohan le Speleologue",
                            "Les anciens peuples d'Eldanior utilisaient les cavernes " +
                            "comme entrepots. Ils y cachaient leurs biens les plus precieux " +
                            "dans des coffres renforces.\n\n" +
                            "J'en ai repere plusieurs, mais il y en a tellement " +
                            "que je ne peux pas tous les atteindre seul.",
                            "Il eclaire une carte des reseaux souterrains"),

                    new DialoguePage("Rohan le Speleologue",
                            "Trouve au moins 12 coffres dans les environs. " +
                            "Grottes, souterrains, cavernes... les profondeurs " +
                            "regorgent de secrets.\n\n" +
                            "Attention aux creatures des tenebres. " +
                            "Elles n'aiment pas qu'on derange leur tranquillite.",
                            "Il ajuste sa lanterne et pointe vers l'entree d'une grotte")
                ),

                null,
                null,
                1440
        );
    }
}
