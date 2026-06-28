package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #39 : Bois de Chene Ancien
 * NPC: Cedric le Bucheron — Recolter 500 wood_oak
 */
public class SecQuest_Recolte_N4 extends NpcDialogueQuest {

    public SecQuest_Recolte_N4() {
        super(
                "sec_recolte_n4",
                "Bois de Chene Ancien",
                "Abattez du chene pour Cedric afin de reconstruire le pont du village.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                "wood_oak", 500,
                1300, 900, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N4",

                List.of(
                    new DialoguePage("Cedric le Bucheron",
                            "Cedric, bucheron. Le charpentier a besoin de cinq cents pieces de chene pour reconstruire le pont."),
                    new DialoguePage("Cedric le Bucheron",
                            "Le pont est vital pour le commerce. Sans lui, le village est coupe de la capitale. C'est urgent.")
                ),

                null,
                null,
                1440
        );
    }
}
