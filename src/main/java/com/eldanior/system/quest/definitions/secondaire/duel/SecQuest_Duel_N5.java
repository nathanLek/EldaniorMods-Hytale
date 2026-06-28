package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N5 : Le Tournoi de Brisefer
 * Difficulte D - Gagner 5 duels
 */
public class SecQuest_Duel_N5 extends NpcDialogueQuest {

    public SecQuest_Duel_N5() {
        super(
                "sec_duel_5",
                "Le Tournoi de Brisefer",
                "L'organisateur du tournoi de Brisefer recrute des combattants.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                null, 5,
                1000, 3500, null,
                null,
                "Quest_Npc_Secondaire_Duel_N5",

                List.of(
                    new DialoguePage("Heraut Brennan",
                            "Oyez, oyez ! Le Grand Tournoi de Brisefer est ouvert !\n\n" +
                            "Chaque annee, les meilleurs combattants du royaume s'affrontent " +
                            "dans notre arene legendaire. Gloire, richesse et renommee " +
                            "attendent les vainqueurs !",
                            "Un heraut en tabard rouge et or souffle dans une corne"),

                    new DialoguePage("Heraut Brennan",
                            "Pour participer, il faut prouver sa valeur. " +
                            "Cinq victoires en duel, voila le droit d'entree.\n\n" +
                            "Les regles sont simples : pas de poison, pas de magie noire, " +
                            "et on se releve apres chaque combat. L'honneur avant tout !",
                            "Il deroule un parchemin couvert de noms"),

                    new DialoguePage("Heraut Brennan",
                            "Inscris ton nom ici, aventurier. Cinq duels gagnes, " +
                            "et tu seras officiellement reconnu comme participant de Brisefer.\n\n" +
                            "Que les dieux guident ta lame !",
                            "Il tend une plume d'oie et un encrier")
                ),
                null, null, 1440
        );
    }
}
