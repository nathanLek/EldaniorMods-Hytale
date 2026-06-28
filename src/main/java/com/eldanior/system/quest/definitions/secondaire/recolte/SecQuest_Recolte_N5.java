package com.eldanior.system.quest.definitions.secondaire.recolte;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #40 : Champignons Lumineux
 * NPC: Willow la Druide — Recolter 20 mushroom_glowing
 */
public class SecQuest_Recolte_N5 extends NpcDialogueQuest {

    public SecQuest_Recolte_N5() {
        super(
                "sec_recolte_n5",
                "Champignons Lumineux",
                "Trouvez des champignons lumineux dans les cavernes pour Willow la Druide.",
                QuestType.RECOLTE, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                "mushroom_glowing", 20,
                2200, 1800, null,
                null,
                "Quest_Npc_Secondaire_Recolte_N5",

                List.of(
                    new DialoguePage("Willow la Druide",
                            "Willow, druide du Cercle Verdoyant. Les champignons lumineux des cavernes possedent des proprietes magiques uniques."),
                    new DialoguePage("Willow la Druide",
                            "Vingt specimens suffiraient pour creer un elixir de guerison permanente. Mais ils ne poussent que dans l'obscurite la plus profonde.")
                ),

                null,
                null,
                1440
        );
    }
}
