package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #48 : Champignons Rares
 * NPC: Coral la Plongeuse — Recolter 50 mushroom
 */
public class SecQuest_Recolte_N13 extends NpcDialogueQuest {

    public SecQuest_Recolte_N13() {
        super(
                "sec_recolte_n13",
                "Champignons Rares",
                "Recoltez des champignons rares des cavernes pour Coral la Plongeuse.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                "mushroom", 50,
                2500, 2000, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N13",

                List.of(
                    new DialoguePage("Coral la Plongeuse",
                            "Coral, cueilleuse des profondeurs. Les champignons des cavernes valent une fortune au marche."),
                    new DialoguePage("Coral la Plongeuse",
                            "Cinquante specimens. Ils poussent dans les grottes les plus sombres. Peu de cueilleurs osent y aller.")
                ),

                null,
                null,
                1440
        );
    }
}
