package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N13 : L'Heritiere de l'Arene
 * Difficulte B - Gagner 15 duels
 */
public class SecQuest_Duel_N13 extends NpcDialogueQuest {

    public SecQuest_Duel_N13() {
        super(
                "sec_duel_13",
                "L'Heritiere de l'Arene",
                "La fille du Grand Maitre de l'arene cherche un protecteur pour sa quete de verite.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                null, 15,
                3000, 12000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N13",

                List.of(
                    new DialoguePage("Elara Sangacier",
                            "Mon pere etait le Grand Maitre de l'Arene Eternelle. " +
                            "Il a ete assassine il y a trois lunes.\n\n" +
                            "Les autorites disent que c'etait un accident. " +
                            "Je sais que c'est un mensonge. Quelqu'un l'a tue " +
                            "pour prendre le controle de l'arene.",
                            "Une jeune femme aux cheveux noirs, le regard determine"),

                    new DialoguePage("Elara Sangacier",
                            "J'ai besoin d'un combattant qui se fasse un nom dans les duels. " +
                            "Quelqu'un qui attire l'attention, qui fasse parler.\n\n" +
                            "Quinze victoires devraient suffire a faire trembler ceux " +
                            "qui se cachent dans l'ombre de l'arene.",
                            "Elle serre un medaillon contre sa poitrine"),

                    new DialoguePage("Elara Sangacier",
                            "Pendant que tu combats, j'enqueterai. Tes victoires seront " +
                            "ma couverture. Plus tu brilles, plus ils se revelent.\n\n" +
                            "Quinze duels, aventurier. Pour la justice et pour mon pere.",
                            "Des larmes brillent dans ses yeux, mais sa voix ne tremble pas")
                ),
                null, null, 1440
        );
    }
}
