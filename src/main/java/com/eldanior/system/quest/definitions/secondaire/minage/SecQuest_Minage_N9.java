package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #29 : Gravier Sans Fin
 * Miner 500 soil_gravel pour Helga la Naine.
 */
public class SecQuest_Minage_N9 extends NpcDialogueQuest {

    public SecQuest_Minage_N9() {
        super(
                "sec_minage_n9",
                "Gravier Sans Fin",
                "Minez 500 blocs de gravier pour Helga la Naine.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                "soil_gravel", 500,
                500, 300, null,
                null,
                "Quest_Npc_Secondaire_Minage_N9",

                List.of(
                    new DialoguePage("Helga la Naine",
                            "Helga ! Les routes sont dans un etat lamentable. Il me faut du gravier pour les reparer."),
                    new DialoguePage("Helga la Naine",
                            "Cinq cents portions devraient suffire pour la route principale. Simple mais necessaire !")
                ),

                null,
                null,
                1440
        );
    }
}
