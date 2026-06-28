package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #46 : Recolte Abondante
 * NPC: Primrose l'Apothicaire — Recolter 500 plant (tout type)
 * Titre: herboriste
 */
public class SecQuest_Recolte_N11 extends NpcDialogueQuest {

    public SecQuest_Recolte_N11() {
        super(
                "sec_recolte_n11",
                "Recolte Abondante",
                "Recoltez cinq cents plantes de tout type pour l'apothicaire Primrose.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                "plant", 500,
                4200, 3500, "herboriste",
                null,
                "Quest_Npc_Secondaire_Recolte_N11",

                List.of(
                    new DialoguePage("Primrose l'Apothicaire",
                            "Primrose, apothicaire. Mon stock est vide, ma boutique est fermee, et les malades affluent."),
                    new DialoguePage("Primrose l'Apothicaire",
                            "Cinq cents plantes de n'importe quel type. Je transformerai tout en potions, onguents et remedes.")
                ),

                null,
                null,
                1440
        );
    }
}
