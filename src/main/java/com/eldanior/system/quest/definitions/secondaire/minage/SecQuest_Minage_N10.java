package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #30 : Adamantite Legendaire
 * Miner 10 ore_adamantite pour Rolf le Dynamiteur.
 * Recompense : titre chercheur_adamantite.
 */
public class SecQuest_Minage_N10 extends NpcDialogueQuest {

    public SecQuest_Minage_N10() {
        super(
                "sec_minage_n10",
                "Adamantite Legendaire",
                "Minez 10 minerais d'adamantite pour Rolf le Dynamiteur.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                "ore_adamantite", 10,
                9000, 7000, "chercheur_adamantite",
                null,
                "Quest_Npc_Secondaire_Minage_N10",

                List.of(
                    new DialoguePage("Rolf le Dynamiteur",
                            "Rolf, dynamiteur de legende. L'adamantite... le metal des dieux. On dit qu'il n'en reste que quelques veines dans le monde entier."),
                    new DialoguePage("Rolf le Dynamiteur",
                            "Dix morceaux. C'est tout ce que je demande. Avec ca, on pourrait forger une armure capable de resister a n'importe quoi.")
                ),

                null,
                null,
                1440
        );
    }
}
