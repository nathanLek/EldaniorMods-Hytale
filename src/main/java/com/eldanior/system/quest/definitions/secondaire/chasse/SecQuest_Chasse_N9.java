package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N9 extends NpcDialogueQuest {
    public SecQuest_Chasse_N9() {
        super(
                "sec_chasse_n9",
                "Esprits Errants",
                "Dissiper les spectres qui hantent la vallee.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                "spectre", 10,
                4500, 3500, null,
                null, "Quest_Npc_Secondaire_Chasse_N9",
                List.of(
                        new DialoguePage("Selene la Druide", "Je suis Selene, gardienne du cercle druidique. Les esprits de la vallee sont en colere. Quelque chose les a corrompus."),
                        new DialoguePage("Selene la Druide", "Ces entites corrompent tout ce qu'elles touchent. Les arbres meurent, les animaux fuient. Si on ne fait rien, la foret entiere perira."),
                        new DialoguePage("Selene la Druide", "Dix spectres doivent etre dissipes pour purifier la vallee. Ce ne sera pas facile, ces creatures sont insaisissables.")
                ),
                null, null, 1440
        );
    }
}
