package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #25 : Cristaux des Profondeurs
 * Miner 100 rock_crystal pour Eira la Cristalliere.
 */
public class SecQuest_Minage_N5 extends NpcDialogueQuest {

    public SecQuest_Minage_N5() {
        super(
                "sec_minage_n5",
                "Cristaux des Profondeurs",
                "Minez 100 cristaux des profondeurs pour Eira la Cristalliere.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                "rock_crystal", 100,
                2500, 2000, null,
                null,
                "Quest_Npc_Secondaire_Minage_N5",

                List.of(
                    new DialoguePage("Eira la Cristalliere",
                            "Eira, cristalliere. Les cristaux des profondeurs sont la source de toute magie dans cette region."),
                    new DialoguePage("Eira la Cristalliere",
                            "Les reserves s'epuisent. Sans cent nouveaux cristaux, nos enchanteurs ne pourront plus rien proteger.")
                ),

                null,
                null,
                1440
        );
    }
}
