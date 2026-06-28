package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N9 : Les Secrets du Gardien.
 * Un gardien de phare solitaire connait les emplacements de coffres caches sur la cote.
 * Difficulte B - 15 coffres a decouvrir.
 */
public class SecQuest_Exploration_N9 extends NpcDialogueQuest {

    public SecQuest_Exploration_N9() {
        super(
                "sec_exploration_9",
                "Les Secrets du Gardien",
                "Un gardien de phare solitaire vous revele les emplacements de coffres caches sur la cote.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                null, 15,
                2800, 8000, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N9",

                List.of(
                    new DialoguePage("Odilon le Gardien",
                            "Depuis mon phare, je vois tout. Les bateaux qui passent, " +
                            "les tempetes qui grondent... et les naufrages.\n\n" +
                            "Tant de navires se sont ecrases contre les recifs. " +
                            "Leurs cargaisons se sont echouees un peu partout le long " +
                            "de la cote.",
                            "Un homme bourru dans un phare battu par les vents"),

                    new DialoguePage("Odilon le Gardien",
                            "J'ai repere des coffres depuis ma tour. Certains sur les plages, " +
                            "d'autres dans les criques, quelques-uns meme dans les falaises.\n\n" +
                            "Je suis trop vieux pour descendre les chercher. " +
                            "Mais toi, tu as l'air solide.",
                            "Il pointe vers la cote en contrebas"),

                    new DialoguePage("Odilon le Gardien",
                            "Ramene-moi la preuve que tu en as trouve 15. " +
                            "Je te donnerai ma recompense, et surtout mes remerciements.\n\n" +
                            "Fais attention aux marees. Certains coffres ne sont " +
                            "accessibles qu'a maree basse... ou qu'en nageant.",
                            "Le vent fait trembler les vitres du phare")
                ),

                null,
                null,
                1440
        );
    }
}
