package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #38 : Bouquet Royal
 * NPC: Rosalie la Fleuriste — Recolter 40 plant_flower
 */
public class SecQuest_Recolte_N3 extends NpcDialogueQuest {

    public SecQuest_Recolte_N3() {
        super(
                "sec_recolte_n3",
                "Bouquet Royal",
                "Cueillez des fleurs pour le mariage royal organise par Rosalie.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                "plant_flower", 40,
                800, 500, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N3",

                List.of(
                    new DialoguePage("Rosalie la Fleuriste",
                            "Rosalie, fleuriste de la cour. Le mariage royal est dans trois jours et je n'ai pas assez de fleurs !"),
                    new DialoguePage("Rosalie la Fleuriste",
                            "Quarante bouquets de fleurs variees. Des roses, des lys, des tulipes... tout ce que tu trouves de beau !")
                ),

                null,
                null,
                1440
        );
    }
}
