package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #32 : Argile des Rivieres
 * Miner 300 soil_clay pour Finn le Terrassier.
 */
public class SecQuest_Minage_N12 extends NpcDialogueQuest {

    public SecQuest_Minage_N12() {
        super(
                "sec_minage_n12",
                "Argile des Rivieres",
                "Minez 300 blocs d'argile pour Finn le Terrassier.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                "soil_clay", 300,
                450, 250, null,
                null,
                "Quest_Npc_Secondaire_Minage_N12",

                List.of(
                    new DialoguePage("Finn le Terrassier",
                            "Finn, terrassier. Le potier du village a besoin d'argile. Beaucoup d'argile."),
                    new DialoguePage("Finn le Terrassier",
                            "Trois cents blocs. On les trouve pres des rivieres. Travail simple mais fatigant !")
                ),

                null,
                null,
                1440
        );
    }
}
