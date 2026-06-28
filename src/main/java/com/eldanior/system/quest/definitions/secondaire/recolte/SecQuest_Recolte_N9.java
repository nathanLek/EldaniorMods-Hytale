package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #44 : Spores Anciennes
 * NPC: Fern la Botaniste — Recolter 30 mushroom_cap_poison
 */
public class SecQuest_Recolte_N9 extends NpcDialogueQuest {

    public SecQuest_Recolte_N9() {
        super(
                "sec_recolte_n9",
                "Spores Anciennes",
                "Recoltez des champignons toxiques pour les recherches de Fern la Botaniste.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                "mushroom_cap_poison", 30,
                1600, 1100, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N9",

                List.of(
                    new DialoguePage("Fern la Botaniste",
                            "Fern, botaniste de l'Academie. Les champignons toxiques sont mal compris. Leur poison peut etre transforme en antidote."),
                    new DialoguePage("Fern la Botaniste",
                            "Trente specimens. Attention a ne pas les inhaler en les recoltant. Ca serait... regrettable.")
                ),

                null,
                null,
                1440
        );
    }
}
