package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #33 : Argent Etincelant
 * Miner 400 ore_silver pour Olga la Fondatrice.
 */
public class SecQuest_Minage_N13 extends NpcDialogueQuest {

    public SecQuest_Minage_N13() {
        super(
                "sec_minage_n13",
                "Argent Etincelant",
                "Minez 400 minerais d'argent pour Olga la Fondatrice.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                "ore_silver", 400,
                900, 600, null,
                null,
                "Quest_Npc_Secondaire_Minage_N13",

                List.of(
                    new DialoguePage("Olga la Fondatrice",
                            "Olga, responsable de la fonderie. Nos reserves d'argent sont au plus bas."),
                    new DialoguePage("Olga la Fondatrice",
                            "Quatre cents minerais. Sans ca, les joailliers ne pourront plus travailler.")
                ),

                null,
                null,
                1440
        );
    }
}
