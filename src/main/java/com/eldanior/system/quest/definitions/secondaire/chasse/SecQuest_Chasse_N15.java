package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N15 extends NpcDialogueQuest {
    public SecQuest_Chasse_N15() {
        super(
                "sec_chasse_n15",
                "Menace Trork",
                "Repousser les guerriers Trork hors de la foret.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                "trork warrior", 12,
                1800, 1200, null,
                null, "Quest_Npc_Secondaire_Chasse_N15",
                List.of(
                        new DialoguePage("Ivy la Forestiere", "Je suis Ivy, garde forestiere depuis quinze ans. La foret est mon domaine et elle est en danger."),
                        new DialoguePage("Ivy la Forestiere", "Douze guerriers Trork ont etabli un avant-poste dans la clairiere nord. Ils abattent les arbres centenaires pour construire des machines de guerre."),
                        new DialoguePage("Ivy la Forestiere", "Si tu peux les repousser, la foret te sera reconnaissante. Et moi aussi.")
                ),
                null, null, 1440
        );
    }
}
