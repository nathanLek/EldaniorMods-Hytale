package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #41 : Baies des Sommets
 * NPC: Jasper le Cueilleur — Recolter 100 plant_crop_berry
 */
public class SecQuest_Recolte_N6 extends NpcDialogueQuest {

    public SecQuest_Recolte_N6() {
        super(
                "sec_recolte_n6",
                "Baies des Sommets",
                "Cueillez des baies des sommets pour la prochaine cuvee de Jasper.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                "plant_crop_berry", 100,
                700, 450, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N6",

                List.of(
                    new DialoguePage("Jasper le Cueilleur",
                            "Jasper, cueilleur professionnel. Les baies des sommets font le meilleur vin de la region."),
                    new DialoguePage("Jasper le Cueilleur",
                            "Cent baies pour la prochaine cuvee. Elles poussent en altitude, faut pas avoir le vertige !")
                ),

                null,
                null,
                1440
        );
    }
}
