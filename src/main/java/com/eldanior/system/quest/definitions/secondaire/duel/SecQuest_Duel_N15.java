package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N15 : La Legende de l'Invaincu
 * Difficulte A - Gagner 15 duels - Donne le titre "Invaincu"
 */
public class SecQuest_Duel_N15 extends NpcDialogueQuest {

    public SecQuest_Duel_N15() {
        super(
                "sec_duel_15",
                "La Legende de l'Invaincu",
                "Un chroniqueur legendaire veut ecrire l'histoire du prochain grand champion.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                null, 15,
                4000, 20000, "invaincu",
                null,
                "Quest_Npc_Secondaire_Duel_N15",

                List.of(
                    new DialoguePage("Chroniqueur Erasmus",
                            "Je suis Erasmus, gardien des chroniques de l'Arene Eternelle. " +
                            "Chaque grand champion a sa page dans mon livre.\n\n" +
                            "Mais cela fait des annees que personne n'a merite une nouvelle entree. " +
                            "Les combattants d'aujourd'hui manquent de panache.",
                            "Un vieil homme entoure de parchemins et de livres"),

                    new DialoguePage("Chroniqueur Erasmus",
                            "J'ai entendu parler de vous. Vos exploits commencent a faire du bruit. " +
                            "Mais pour entrer dans mon livre, il faut plus que du talent.\n\n" +
                            "Il faut la Legende. Quinze victoires en duel, sans faillir, " +
                            "sans tricher, sans faiblir. Voila ce qui fait un Invaincu.",
                            "Il ouvre un livre aux pages dorees"),

                    new DialoguePage("Chroniqueur Erasmus",
                            "Le titre d'Invaincu n'a ete porte que par sept personnes " +
                            "dans toute l'histoire d'Eldanior. Serez-vous le huitieme ?\n\n" +
                            "Quinze duels, aventurier. L'histoire vous attend.",
                            "Il trempe sa plume dans l'encre, pret a ecrire")
                ),
                null, null, 1440
        );
    }
}
