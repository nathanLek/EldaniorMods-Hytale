package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N17 extends NpcDialogueQuest {
    public SecQuest_Chasse_N17() {
        super(
                "sec_chasse_n17",
                "Purge Sacree",
                "Purifier la region en eliminant cent creatures corrompues.",
                QuestType.MASSACRE, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                null, 100,
                6000, 5000, "purificateur",
                null, "Quest_Npc_Secondaire_Chasse_N17",
                List.of(
                        new DialoguePage("Freya la Valkyrie", "Je suis Freya, derniere Valkyrie du sanctuaire celeste. Les dieux m'ont envoyee pour purifier cette terre souillée."),
                        new DialoguePage("Freya la Valkyrie", "Cent creatures corrompues doivent tomber pour que la terre retrouve sa purete. Peu importe leur espece, c'est la corruption en elles qui doit etre detruite."),
                        new DialoguePage("Freya la Valkyrie", "Celui qui accomplira cette purge sacree recevra le titre de Purificateur. Les dieux eux-memes reconnaitront ta valeur.")
                ),
                null, null, 1440
        );
    }
}
