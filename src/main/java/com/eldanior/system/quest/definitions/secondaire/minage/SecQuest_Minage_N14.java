package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #34 : Craie des Falaises
 * Miner 250 rock_chalk pour Bram le Sculpteur.
 */
public class SecQuest_Minage_N14 extends NpcDialogueQuest {

    public SecQuest_Minage_N14() {
        super(
                "sec_minage_n14",
                "Craie des Falaises",
                "Minez 250 blocs de craie pour Bram le Sculpteur.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                "rock_chalk", 250,
                550, 350, null,
                null,
                "Quest_Npc_Secondaire_Minage_N14",

                List.of(
                    new DialoguePage("Bram le Sculpteur",
                            "Bram, sculpteur. J'ai une commande urgente du seigneur local. Il veut une fresque en craie pour sa grande salle."),
                    new DialoguePage("Bram le Sculpteur",
                            "Deux cent cinquante blocs de craie des falaises. Douce, blanche, parfaite pour la sculpture.")
                ),

                null,
                null,
                1440
        );
    }
}
