package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N2 : Sang et Acier
 * Difficulte F - Gagner 3 duels
 */
public class SecQuest_Duel_N2 extends NpcDialogueQuest {

    public SecQuest_Duel_N2() {
        super(
                "sec_duel_2",
                "Sang et Acier",
                "Une ancienne gladiatrice cherche des combattants prometteurs.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.F,
                null, 3,
                400, 1000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N2",

                List.of(
                    new DialoguePage("Lyra la Cicatrice",
                            "Tu vois ces marques sur mon visage ? Chacune raconte une histoire. " +
                            "Chacune m'a rendue plus forte.\n\n" +
                            "Dans l'arene de Karthage, j'ai combattu pendant quinze ans. " +
                            "J'ai survecu a plus de duels que tu n'as mange de repas chauds.",
                            "Une femme couverte de cicatrices, au regard perçant"),

                    new DialoguePage("Lyra la Cicatrice",
                            "Aujourd'hui je cherche la prochaine generation de combattants. " +
                            "Pas des brutes epaisses, non. Des guerriers qui pensent.\n\n" +
                            "Gagne trois duels. Pas un de plus, pas un de moins. " +
                            "Montre-moi que tu sais t'adapter a differents adversaires.",
                            "Elle fait tourner une dague entre ses doigts"),

                    new DialoguePage("Lyra la Cicatrice",
                            "Trois victoires, et je te transmettrai un peu de ce que l'arene m'a appris. " +
                            "Echoue, et ne te represente pas devant moi.\n\n" +
                            "L'acier ne ment jamais.",
                            "Elle tourne les talons et s'eloigne")
                ),
                null, null, 1440
        );
    }
}
