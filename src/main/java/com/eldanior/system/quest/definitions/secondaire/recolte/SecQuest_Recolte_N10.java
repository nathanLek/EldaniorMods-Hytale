package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #45 : Bois Petrifie
 * NPC: Moss le Sylvain — Recolter 200 wood_petrified
 */
public class SecQuest_Recolte_N10 extends NpcDialogueQuest {

    public SecQuest_Recolte_N10() {
        super(
                "sec_recolte_n10",
                "Bois Petrifie",
                "Rassemblez du bois petrifie pour les armures des eclaireurs de Moss.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                "wood_petrified", 200,
                2800, 2300, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N10",

                List.of(
                    new DialoguePage("Moss le Sylvain",
                            "Moss, gardien sylvain. Le bois petrifie est un materiau ancien, aussi dur que la pierre mais leger comme le bois."),
                    new DialoguePage("Moss le Sylvain",
                            "Deux cents morceaux. On en fera des armures legeres pour nos eclaireurs. Ca leur sauvera la vie.")
                ),

                null,
                null,
                1440
        );
    }
}
