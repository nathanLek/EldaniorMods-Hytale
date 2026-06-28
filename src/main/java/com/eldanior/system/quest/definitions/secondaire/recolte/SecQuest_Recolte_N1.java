package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #36 : Herbes Medicinales
 * NPC: Flora l'Herboriste — Recolter 30 plant_crop_health
 */
public class SecQuest_Recolte_N1 extends NpcDialogueQuest {

    public SecQuest_Recolte_N1() {
        super(
                "sec_recolte_n1",
                "Herbes Medicinales",
                "Recoltez des plantes medicinales pour Flora l'Herboriste.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.F,
                "plant_crop_health", 30,
                250, 150, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N1",

                List.of(
                    new DialoguePage("Flora l'Herboriste",
                            "Flora, herboriste du village. L'epidemie de fievre vide mes reserves. J'ai besoin de plantes medicinales."),
                    new DialoguePage("Flora l'Herboriste",
                            "Trente plants suffiront pour preparer assez de remedes. Tu les trouveras dans les prairies.")
                ),

                null,
                null,
                1440
        );
    }
}
