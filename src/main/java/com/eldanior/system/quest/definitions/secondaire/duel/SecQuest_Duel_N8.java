package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N8 : La Fille du Forgeron
 * Difficulte C - Gagner 5 duels
 */
public class SecQuest_Duel_N8 extends NpcDialogueQuest {

    public SecQuest_Duel_N8() {
        super(
                "sec_duel_8",
                "La Fille du Forgeron",
                "Une jeune forgeronne veut prouver que ses lames sont les meilleures du marche.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                null, 5,
                1000, 4000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N8",

                List.of(
                    new DialoguePage("Hilda Flammeforge",
                            "Mon pere dit que mes lames sont trop fines, trop legeres. " +
                            "Il dit qu'une vraie epee doit peser comme un boeuf mort.\n\n" +
                            "Absurde ! La finesse de l'acier vaut mieux que le poids brut. " +
                            "Et je vais le prouver.",
                            "Une jeune femme aux bras muscles, couverte de suie"),

                    new DialoguePage("Hilda Flammeforge",
                            "J'ai besoin d'un cobaye... enfin, d'un partenaire. " +
                            "Prends mes lames et gagne cinq duels avec.\n\n" +
                            "Si tu triomphes, la qualite de mon travail sera demontree. " +
                            "Et tu garderas une belle recompense, evidemment.",
                            "Elle presente une lame finement ouvragee"),

                    new DialoguePage("Hilda Flammeforge",
                            "Cinq victoires, et le vieux grognon devra reconnaitre mon talent. " +
                            "Ne casse pas mes lames, hein !\n\n" +
                            "Allez, montre-leur ce que l'acier de Flammeforge peut faire !",
                            "Elle frappe l'enclume avec un sourire feroce")
                ),
                null, null, 1440
        );
    }
}
