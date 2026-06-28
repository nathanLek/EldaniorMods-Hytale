package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #26 : Cobalt Rare
 * Miner 50 ore_cobalt pour Volkan l'Artificier.
 */
public class SecQuest_Minage_N6 extends NpcDialogueQuest {

    public SecQuest_Minage_N6() {
        super(
                "sec_minage_n6",
                "Cobalt Rare",
                "Minez 50 minerais de cobalt pour Volkan l'Artificier.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                "ore_cobalt", 50,
                4000, 3500, null,
                null,
                "Quest_Npc_Secondaire_Minage_N6",

                List.of(
                    new DialoguePage("Volkan l'Artificier",
                            "Volkan, artificier royal. Je travaille sur une arme qui pourrait changer le cours de la guerre."),
                    new DialoguePage("Volkan l'Artificier",
                            "Mais il me faut du cobalt. Cinquante minerais, pas un de moins. Ce metal est aussi rare que precieux.")
                ),

                null,
                null,
                1440
        );
    }
}
