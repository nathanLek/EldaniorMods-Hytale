package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #47 : Bois de Bouleau
 * NPC: Reed le Bucheron — Recolter 300 wood_birch
 */
public class SecQuest_Recolte_N12 extends NpcDialogueQuest {

    public SecQuest_Recolte_N12() {
        super(
                "sec_recolte_n12",
                "Bois de Bouleau",
                "Abattez du bouleau pour les meubles commandes par le seigneur local.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                "wood_birch", 300,
                900, 600, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N12",

                List.of(
                    new DialoguePage("Reed le Bucheron",
                            "Reed, bucheron du nord. Le bouleau est le bois prefere des artisans pour les meubles fins."),
                    new DialoguePage("Reed le Bucheron",
                            "Trois cents troncs de bouleau. Le seigneur veut remeubler tout son chateau !")
                ),

                null,
                null,
                1440
        );
    }
}
