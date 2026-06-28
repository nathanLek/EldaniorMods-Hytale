package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #27 : Thorium Abyssal
 * Miner 25 ore_thorium pour Ingrid la Maitre-Mine.
 * Recompense : titre maitre_mineur.
 */
public class SecQuest_Minage_N7 extends NpcDialogueQuest {

    public SecQuest_Minage_N7() {
        super(
                "sec_minage_n7",
                "Thorium Abyssal",
                "Minez 25 minerais de thorium pour Ingrid la Maitre-Mine.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                "ore_thorium", 25,
                7000, 6000, "maitre_mineur",
                null,
                "Quest_Npc_Secondaire_Minage_N7",

                List.of(
                    new DialoguePage("Ingrid la Maitre-Mine",
                            "Ingrid, maitre des mines. En quarante ans de carriere, je n'ai extrait que trois morceaux de thorium."),
                    new DialoguePage("Ingrid la Maitre-Mine",
                            "Ce minerai ne se trouve que dans les abysses les plus dangereuses. Vingt-cinq morceaux... c'est un exploit que personne n'a realise.")
                ),

                null,
                null,
                1440
        );
    }
}
