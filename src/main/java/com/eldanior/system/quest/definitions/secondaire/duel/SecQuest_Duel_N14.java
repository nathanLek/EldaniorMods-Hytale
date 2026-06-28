package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N14 : Le Serment du Gladiateur
 * Difficulte B - Gagner 15 duels
 */
public class SecQuest_Duel_N14 extends NpcDialogueQuest {

    public SecQuest_Duel_N14() {
        super(
                "sec_duel_14",
                "Le Serment du Gladiateur",
                "Un gladiateur retraite transmet le serment sacre de l'arene aux combattants meritants.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                null, 15,
                3500, 14000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N14",

                List.of(
                    new DialoguePage("Magnus le Colosse",
                            "Avant chaque combat, nous prononcions le Serment. " +
                            "Des mots anciens, graves dans la pierre de la premiere arene.\n\n" +
                            "'Par le sang verse et l'acier trempe, je jure de combattre " +
                            "avec honneur, de vaincre sans honte, et de tomber sans regret.'",
                            "Un geant couvert de tatouages rituels"),

                    new DialoguePage("Magnus le Colosse",
                            "Ce serment n'est pas que des mots. C'est un mode de vie. " +
                            "Ceux qui le prononcent deviennent freres et soeurs d'arene.\n\n" +
                            "Mais pour le meriter, il faut prouver sa valeur. " +
                            "Quinze victoires en duel. Chacune forgera ton ame " +
                            "comme le marteau forge l'acier.",
                            "Il montre les cicatrices sur ses avant-bras"),

                    new DialoguePage("Magnus le Colosse",
                            "Quand tu reviendras victorieux, je t'enseignerai le Serment " +
                            "dans son integralite. Et tu rejoindras notre fraternite.\n\n" +
                            "Va, et combats avec tout ton coeur.",
                            "Il pose son poing sur sa poitrine en signe de respect")
                ),
                null, null, 1440
        );
    }
}
