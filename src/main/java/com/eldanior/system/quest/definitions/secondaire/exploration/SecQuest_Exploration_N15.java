package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N15 : Le Grand Inventaire.
 * Le conservateur de la Grande Bibliotheque d'Eldanior lance un inventaire monumental.
 * Difficulte S - 40 coffres a decouvrir.
 */
public class SecQuest_Exploration_N15 extends NpcDialogueQuest {

    public SecQuest_Exploration_N15() {
        super(
                "sec_exploration_15",
                "Le Grand Inventaire",
                "Le conservateur de la Grande Bibliotheque vous charge d'un inventaire monumental des coffres d'Eldanior.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                null, 40,
                10000, 50000, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N15",

                List.of(
                    new DialoguePage("Conservateur Lysandre",
                            "Bienvenue dans la Grande Bibliotheque d'Eldanior. " +
                            "Je suis Lysandre, son conservateur depuis trente ans.\n\n" +
                            "Nous avons un projet ambitieux : cataloguer chaque " +
                            "coffre existant dans le monde connu. Un travail titanesque, " +
                            "mais essentiel pour la preservation du savoir.",
                            "Un homme maigre aux lunettes epaisses, entoure de livres"),

                    new DialoguePage("Conservateur Lysandre",
                            "Nos archives indiquent que des centaines de coffres " +
                            "sont disperses a travers Eldanior. Vestiges d'empires, " +
                            "caches de brigands, tresors oublies...\n\n" +
                            "Chaque coffre decouvert enrichit notre connaissance " +
                            "de l'histoire de ce monde. C'est une mission " +
                            "de la plus haute importance.",
                            "Il vous montre des etageres remplies de registres"),

                    new DialoguePage("Conservateur Lysandre",
                            "Je vous demande de decouvrir 40 coffres. " +
                            "C'est considerable, j'en conviens. Mais la Bibliotheque " +
                            "saura se montrer genereuse.\n\n" +
                            "Or, experience, et surtout... la reconnaissance eternelle " +
                            "de la plus grande institution academique d'Eldanior.\n\n" +
                            "Partez, explorez, et revenez avec vos decouvertes.",
                            "Il trempe sa plume dans l'encre et ouvre un nouveau registre")
                ),

                null,
                null,
                1440
        );
    }
}
