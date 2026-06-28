package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N4 extends NpcDialogueQuest {
    public SecQuest_Chasse_N4() {
        super(
                "sec_chasse_n4",
                "Patrouille Squelette",
                "Detruire les squelettes qui bloquent les routes commerciales.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                "skeleton", 20,
                900, 600, null,
                null, "Quest_Npc_Secondaire_Chasse_N4",
                List.of(
                        new DialoguePage("Sylas l'Eclaireur", "Eclaireur Sylas, au rapport. La situation est critique sur la route du nord. Des hordes de squelettes ont investi les ruines et attaquent tout ce qui bouge."),
                        new DialoguePage("Sylas l'Eclaireur", "Les marchands refusent de passer et le commerce est au point mort. Si ca continue, le village va manquer de provisions."),
                        new DialoguePage("Sylas l'Eclaireur", "Vingt squelettes, c'est ce qu'il faut eliminer pour securiser le passage. Tu es partant ?")
                ),
                null, null, 1440
        );
    }
}
