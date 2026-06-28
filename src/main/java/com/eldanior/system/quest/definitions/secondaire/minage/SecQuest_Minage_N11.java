package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #31 : Marbre Blanc
 * Miner 200 rock_marble pour Astrid la Cartographe.
 */
public class SecQuest_Minage_N11 extends NpcDialogueQuest {

    public SecQuest_Minage_N11() {
        super(
                "sec_minage_n11",
                "Marbre Blanc",
                "Minez 200 blocs de marbre pour Astrid la Cartographe.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                "rock_marble", 200,
                1400, 900, null,
                null,
                "Quest_Npc_Secondaire_Minage_N11",

                List.of(
                    new DialoguePage("Astrid la Cartographe",
                            "Astrid, cartographe. Le sculpteur royal m'envoie chercher du marbre blanc pour le nouveau monument."),
                    new DialoguePage("Astrid la Cartographe",
                            "Deux cents blocs de marbre pur. Pas de fissures, pas de taches. La qualite est essentielle.")
                ),

                null,
                null,
                1440
        );
    }
}
