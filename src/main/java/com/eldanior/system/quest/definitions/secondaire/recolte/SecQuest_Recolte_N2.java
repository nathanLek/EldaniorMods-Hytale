package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #37 : Moisson d'Automne
 * NPC: Gavin le Fermier — Recolter 50 plant_crop_wheat
 */
public class SecQuest_Recolte_N2 extends NpcDialogueQuest {

    public SecQuest_Recolte_N2() {
        super(
                "sec_recolte_n2",
                "Moisson d'Automne",
                "Aidez Gavin le Fermier a recolter du ble pour l'hiver.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                "plant_crop_wheat", 50,
                500, 300, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N2",

                List.of(
                    new DialoguePage("Gavin le Fermier",
                            "Gavin, fermier. L'automne approche et je suis seul pour la moisson. Ma femme est malade, mes enfants trop jeunes."),
                    new DialoguePage("Gavin le Fermier",
                            "Cinquante gerbes de ble. C'est ce qu'il faut pour passer l'hiver. Je te paierai ce que je peux.")
                ),

                null,
                null,
                1440
        );
    }
}
