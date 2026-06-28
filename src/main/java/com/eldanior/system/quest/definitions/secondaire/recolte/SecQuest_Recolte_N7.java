package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #42 : Ingredients Mystiques
 * NPC: Hazel la Sorciere — Recolter 15 plant_crop
 */
public class SecQuest_Recolte_N7 extends NpcDialogueQuest {

    public SecQuest_Recolte_N7() {
        super(
                "sec_recolte_n7",
                "Ingredients Mystiques",
                "Trouvez des plantes rares pour le rituel de protection de Hazel.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                "plant_crop", 15,
                3500, 3000, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N7",

                List.of(
                    new DialoguePage("Hazel la Sorciere",
                            "Je suis Hazel. Certains m'appellent sorciere, mais je prefere... herboriste avancee."),
                    new DialoguePage("Hazel la Sorciere",
                            "J'ai besoin de quinze plantes rares pour un rituel de protection. Le genre de plantes qui mordent quand on les cueille.")
                ),

                null,
                null,
                1440
        );
    }
}
