package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #35 : Le Grand Terrassement
 * Miner 5000 blocs (tout type) pour Thane l'Excavateur.
 * targetId = null signifie "tout type de bloc mine compte".
 * Recompense : titre terrasseur.
 */
public class SecQuest_Minage_N15 extends NpcDialogueQuest {

    public SecQuest_Minage_N15() {
        super(
                "sec_minage_n15",
                "Le Grand Terrassement",
                "Minez 5000 blocs de tout type pour Thane l'Excavateur.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                null, 5000,
                5500, 4500, "terrasseur",
                null,
                "Quest_Npc_Secondaire_Minage_N15",

                List.of(
                    new DialoguePage("Thane l'Excavateur",
                            "Thane, excavateur en chef. Le roi veut un nouveau tunnel sous la montagne. Le plus grand projet de genie civil jamais entrepris."),
                    new DialoguePage("Thane l'Excavateur",
                            "Cinq mille blocs. N'importe quel type de roche. L'important c'est de creuser, creuser, et encore creuser.")
                ),

                null,
                null,
                1440
        );
    }
}
