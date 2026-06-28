package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #50 : La Recolte Ultime
 * NPC: Sage l'Ermite — Recolter 1000 de tout type (targetId = null)
 * Titre: grand_recolteur
 */
public class SecQuest_Recolte_N15 extends NpcDialogueQuest {

    public SecQuest_Recolte_N15() {
        super(
                "sec_recolte_n15",
                "La Recolte Ultime",
                "Recoltez mille offrandes de la terre pour prouver votre respect de la nature.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                null, 1000,
                8000, 6500, "grand_recolteur",
                null,
                "Quest_Npc_Secondaire_Recolte_N15",

                List.of(
                    new DialoguePage("Sage l'Ermite",
                            "Je suis Sage. Ermite. Philosophe. J'observe la nature depuis des decennies et j'ai compris une chose."),
                    new DialoguePage("Sage l'Ermite",
                            "Celui qui recolte mille offrandes de la terre merite le respect de la nature elle-meme. Bois, plante... tout compte.")
                ),

                null,
                null,
                1440
        );
    }
}
