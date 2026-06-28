package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N10 : Le Champion de Valcrest
 * Difficulte C - Gagner 10 duels - Donne le titre "Champion de Valcrest"
 */
public class SecQuest_Duel_N10 extends NpcDialogueQuest {

    public SecQuest_Duel_N10() {
        super(
                "sec_duel_10",
                "Le Champion de Valcrest",
                "Le gouverneur de Valcrest cherche un champion pour representer sa cite.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                null, 10,
                2000, 8000, "champion_valcrest",
                null,
                "Quest_Npc_Secondaire_Duel_N10",

                List.of(
                    new DialoguePage("Gouverneur Harlan",
                            "Valcrest a besoin d'un champion. Quelqu'un qui incarne " +
                            "la force et l'honneur de notre cite.\n\n" +
                            "Les autres villes ont leurs heros, leurs legendes. " +
                            "Nous, nous n'avons que des marchands et des paysans. " +
                            "Il est temps que cela change.",
                            "Un homme d'age mur en habits nobles, l'air preoccupe"),

                    new DialoguePage("Gouverneur Harlan",
                            "Je vous demande de remporter dix duels au nom de Valcrest. " +
                            "Chaque victoire sera annoncee dans toute la region.\n\n" +
                            "Votre nom deviendra synonyme de notre cite. " +
                            "Et en retour, je vous accorderai le titre officiel " +
                            "de Champion de Valcrest.",
                            "Il montre un blason dore accroche au mur"),

                    new DialoguePage("Gouverneur Harlan",
                            "Ce titre n'est pas une simple decoration. Il vous ouvre des portes, " +
                            "vous donne du respect, et surtout... une belle bourse.\n\n" +
                            "Alors, acceptez-vous de porter les couleurs de Valcrest ?",
                            "Il vous tend une echarpe aux couleurs de la cite")
                ),
                null, null, 1440
        );
    }
}
