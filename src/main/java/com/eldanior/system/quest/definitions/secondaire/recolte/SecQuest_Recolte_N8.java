package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #43 : Baies Hivernales
 * NPC: Alden le Vigneron — Recolter 80 plant_crop_berry
 */
public class SecQuest_Recolte_N8 extends NpcDialogueQuest {

    public SecQuest_Recolte_N8() {
        super(
                "sec_recolte_n8",
                "Baies Hivernales",
                "Aidez Alden le Vigneron a recolter des baies pour sa prochaine cuvee.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                "plant_crop_berry", 80,
                450, 280, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N8",

                List.of(
                    new DialoguePage("Alden le Vigneron",
                            "Alden, vigneron. La saison des vendanges bat son plein mais mes ouvriers sont partis chercher fortune ailleurs."),
                    new DialoguePage("Alden le Vigneron",
                            "Quatre-vingts baies. Mon vin ne se fera pas tout seul !")
                ),

                null,
                null,
                1440
        );
    }
}
