package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N7 extends NpcDialogueQuest {
    public SecQuest_Chasse_N7() {
        super(
                "sec_chasse_n7",
                "Araignees des Cavernes",
                "Nettoyer les cavernes infestees d'araignees.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                "crawler", 30,
                2500, 2000, null,
                null, "Quest_Npc_Secondaire_Chasse_N7",
                List.of(
                        new DialoguePage("Lina la Piegeuse", "Lina, piegeuse professionnelle. Et crois-moi, j'en ai vu des bestioles dans ma carriere. Mais ce qui se passe dans ces cavernes depasse tout."),
                        new DialoguePage("Lina la Piegeuse", "Les cavernes sous la montagne sont envahies par des crawlers. Ils tissent leurs toiles partout et les mineurs ne peuvent plus travailler."),
                        new DialoguePage("Lina la Piegeuse", "Trente, c'est le minimum pour degager les galeries principales. Mes pieges ne suffisent plus, il faut y aller a la force brute.")
                ),
                null, null, 1440
        );
    }
}
