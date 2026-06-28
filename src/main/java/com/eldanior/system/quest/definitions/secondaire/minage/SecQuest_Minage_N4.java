package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #24 : Filons d'Or
 * Miner 150 ore_gold pour Silas le Prospecteur.
 */
public class SecQuest_Minage_N4 extends NpcDialogueQuest {

    public SecQuest_Minage_N4() {
        super(
                "sec_minage_n4",
                "Filons d'Or",
                "Minez 150 pepites d'or pour Silas le Prospecteur.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                "ore_gold", 150,
                1500, 1000, null,
                null,
                "Quest_Npc_Secondaire_Minage_N4",

                List.of(
                    new DialoguePage("Silas le Prospecteur",
                            "Silas, prospecteur independant. J'ai trouve un filon d'or gigantesque, mais je suis trop vieux pour miner."),
                    new DialoguePage("Silas le Prospecteur",
                            "Cent cinquante pepites d'or, et on partage les benefices. L'emplacement reste secret, evidemment.")
                ),

                null,
                null,
                1440
        );
    }
}
