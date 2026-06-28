package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N6 : Les Caches du Deserteur.
 * Un ancien soldat deserteur a dissimule des reserves un peu partout.
 * Difficulte D - 10 coffres a decouvrir.
 */
public class SecQuest_Exploration_N6 extends NpcDialogueQuest {

    public SecQuest_Exploration_N6() {
        super(
                "sec_exploration_6",
                "Les Caches du Deserteur",
                "Un ancien soldat deserteur vous revele l'existence de reserves cachees.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                null, 10,
                1200, 3500, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N6",

                List.of(
                    new DialoguePage("Torven le Deserteur",
                            "Je ne suis pas fier de mon passe, aventurier. " +
                            "J'ai deserte l'armee d'Eldanior il y a des annees. " +
                            "Quand on fuit, on apprend vite a cacher ses affaires.\n\n" +
                            "J'ai dissimule des coffres de provisions un peu partout " +
                            "sur ma route de fuite.",
                            "Un homme en haillons au regard hante"),

                    new DialoguePage("Torven le Deserteur",
                            "Aujourd'hui, je n'ai plus besoin de ces reserves. " +
                            "Mais elles sont toujours la, quelque part.\n\n" +
                            "Je ne me souviens plus de tous les emplacements, " +
                            "mais je sais que j'en ai laisse beaucoup. " +
                            "Des dizaines, peut-etre.",
                            "Il dessine vaguement dans la poussiere"),

                    new DialoguePage("Torven le Deserteur",
                            "Si tu en trouves au moins 10, reviens me le dire. " +
                            "Ca me soulagera de savoir qu'elles n'ont pas pourri " +
                            "pour rien.\n\n" +
                            "Et je te donnerai ce qu'il me reste d'or. " +
                            "C'est le moins que je puisse faire.",
                            "Il detourne le regard, pensif")
                ),

                null,
                null,
                1440
        );
    }
}
