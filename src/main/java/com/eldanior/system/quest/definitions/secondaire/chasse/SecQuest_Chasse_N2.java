package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N2 extends NpcDialogueQuest {
    public SecQuest_Chasse_N2() {
        super(
                "sec_chasse_n2",
                "Nid de Scorpions",
                "Exterminer les scaraks qui infestent la region.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                "scarak", 15,
                800, 500, null,
                null, "Quest_Npc_Secondaire_Chasse_N2",
                List.of(
                        new DialoguePage("Elara la Chasseuse", "Psst ! Toi la, t'as l'air de savoir te battre. Je suis Elara, chasseuse de profession."),
                        new DialoguePage("Elara la Chasseuse", "Ces sales bestioles se multiplient a une vitesse folle. Les scaraks envahissent les plaines et personne n'ose s'en approcher."),
                        new DialoguePage("Elara la Chasseuse", "J'ai besoin de quelqu'un pour m'aider a en reduire le nombre. Quinze devraient suffire pour calmer la situation.")
                ),
                null, null, 1440
        );
    }
}
