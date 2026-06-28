package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #49 : Resine d'Ambre
 * NPC: Bramble le Garde-Foret — Recolter 100 wood_amber
 */
public class SecQuest_Recolte_N14 extends NpcDialogueQuest {

    public SecQuest_Recolte_N14() {
        super(
                "sec_recolte_n14",
                "Resine d'Ambre",
                "Recoltez du bois d'ambre pour fortifier la palissade du village.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                "wood_amber", 100,
                1400, 950, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N14",

                List.of(
                    new DialoguePage("Bramble le Garde-Foret",
                            "Bramble, garde-foret. Le bois d'ambre est le plus precieux de la foret. Il brille la nuit et repousse les morts-vivants."),
                    new DialoguePage("Bramble le Garde-Foret",
                            "Cent morceaux pour fortifier le mur de la palissade. Les nuits deviennent dangereuses.")
                ),

                null,
                null,
                1440
        );
    }
}
