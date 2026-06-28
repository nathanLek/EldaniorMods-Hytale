package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N5 extends NpcDialogueQuest {
    public SecQuest_Chasse_N5() {
        super(
                "sec_chasse_n5",
                "Invasion Gobeline",
                "Repousser l'invasion gobeline qui menace la frontiere.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                "goblin", 25,
                1500, 1000, null,
                null, "Quest_Npc_Secondaire_Chasse_N5",
                List.of(
                        new DialoguePage("Kira la Sentinelle", "Sentinelle Kira, en poste depuis trois jours sans releve. Les gobelins ont installe un campement a la lisiere de la foret et ils sont de plus en plus audacieux."),
                        new DialoguePage("Kira la Sentinelle", "On a essaye de negocier, mais ils ne comprennent que la force. Leurs raids nocturnes deviennent insupportables pour les habitants."),
                        new DialoguePage("Kira la Sentinelle", "Elimine au moins vingt-cinq de ces pillards et on pourra enfin respirer. La prime est consequente.")
                ),
                null, null, 1440
        );
    }
}
