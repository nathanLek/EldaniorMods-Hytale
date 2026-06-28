package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N11 : La Memoire des Pierres.
 * Un druide ancien guide le joueur vers des coffres sacres proteges par la nature.
 * Difficulte A - 20 coffres a decouvrir.
 */
public class SecQuest_Exploration_N11 extends NpcDialogueQuest {

    public SecQuest_Exploration_N11() {
        super(
                "sec_exploration_11",
                "La Memoire des Pierres",
                "Un druide ancien vous guide vers des coffres sacres dissimules par la nature elle-meme.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                null, 20,
                4000, 15000, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N11",

                List.of(
                    new DialoguePage("Druide Thalion",
                            "La terre se souvient de tout, jeune mortel. " +
                            "Chaque pas, chaque souffle, chaque tresor enfoui.\n\n" +
                            "Je suis Thalion, gardien des forets ancestrales. " +
                            "Les arbres me murmurent des secrets que les hommes " +
                            "ont oublies depuis longtemps.",
                            "Un vieil homme couvert de mousse et de lierre"),

                    new DialoguePage("Druide Thalion",
                            "Avant le premier age, les Anciens ont cache des reliques " +
                            "dans des coffres proteges par la magie de la terre. " +
                            "Ces coffres sont toujours la, absorbes par la nature.\n\n" +
                            "Les racines les enlacent, la mousse les recouvre, " +
                            "mais ils ne se deteriorent jamais.",
                            "Des lucioles dansent autour de ses mains"),

                    new DialoguePage("Druide Thalion",
                            "Ouvre tes sens a la terre, explorateur. " +
                            "Ecoute le murmure des pierres et des racines. " +
                            "Ils te guideront vers les coffres.\n\n" +
                            "Trouve-en 20. C'est le nombre sacre " +
                            "des gardiens de la foret. Reviens me voir ensuite.",
                            "Les arbres semblent s'ecarter pour vous laisser passer")
                ),

                null,
                null,
                1440
        );
    }
}
