package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #23 : Le Fer des Montagnes
 * Miner 300 ore_iron pour Magnar le Forgeron.
 */
public class SecQuest_Minage_N3 extends NpcDialogueQuest {

    public SecQuest_Minage_N3() {
        super(
                "sec_minage_n3",
                "Le Fer des Montagnes",
                "Minez 300 minerais de fer pour Magnar le Forgeron.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                "ore_iron", 300,
                1000, 700, null,
                null,
                "Quest_Npc_Secondaire_Minage_N3",

                List.of(
                    new DialoguePage("Magnar le Forgeron",
                            "Magnar, forgeron. Ma forge est froide et mes stocks sont vides. Les caravanes de minerai n'arrivent plus."),
                    new DialoguePage("Magnar le Forgeron",
                            "Trois cents lingots de fer. C'est ce qu'il me faut pour armer la garnison. Le temps presse.")
                ),

                null,
                null,
                1440
        );
    }
}
