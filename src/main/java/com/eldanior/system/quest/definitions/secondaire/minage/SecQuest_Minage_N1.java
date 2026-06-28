package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #21 : Premiers Coups de Pioche
 * Miner 500 rock_stone pour Durgan le Mineur.
 */
public class SecQuest_Minage_N1 extends NpcDialogueQuest {

    public SecQuest_Minage_N1() {
        super(
                "sec_minage_n1",
                "Premiers Coups de Pioche",
                "Minez 500 blocs de pierre pour Durgan le Mineur.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.F,
                "rock_stone", 500,
                300, 200, null,
                null,
                "Quest_Npc_Secondaire_Minage_N1",

                List.of(
                    new DialoguePage("Durgan le Mineur",
                            "Durgan, mineur depuis toujours. On manque de pierre pour les fondations du nouveau mur d'enceinte."),
                    new DialoguePage("Durgan le Mineur",
                            "Cinq cents blocs de pierre, c'est ce qu'il nous faut. Tu sais manier une pioche ?")
                ),

                null,
                null,
                1440
        );
    }
}
