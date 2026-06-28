package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #28 : Mille Pierres
 * Miner 1000 rock_stone pour Gorm le Tailleur.
 */
public class SecQuest_Minage_N8 extends NpcDialogueQuest {

    public SecQuest_Minage_N8() {
        super(
                "sec_minage_n8",
                "Mille Pierres",
                "Minez 1000 blocs de pierre pour Gorm le Tailleur.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                "rock_stone", 1000,
                1200, 800, null,
                null,
                "Quest_Npc_Secondaire_Minage_N8",

                List.of(
                    new DialoguePage("Gorm le Tailleur",
                            "Gorm, tailleur de pierre. La cathedrale a besoin de reparations urgentes. Mille blocs de pierre, minimum."),
                    new DialoguePage("Gorm le Tailleur",
                            "C'est un travail colossal, je sais. Mais chaque bloc compte pour sauver ce monument.")
                ),

                null,
                null,
                1440
        );
    }
}
