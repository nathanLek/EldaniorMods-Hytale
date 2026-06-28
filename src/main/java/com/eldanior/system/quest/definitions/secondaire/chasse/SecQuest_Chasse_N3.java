package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N3 extends NpcDialogueQuest {
    public SecQuest_Chasse_N3() {
        super(
                "sec_chasse_n3",
                "L'Ours des Cimes",
                "Eliminer les ours qui rodent dans les montagnes.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                "bear", 5,
                1200, 800, null,
                null, "Quest_Npc_Secondaire_Chasse_N3",
                List.of(
                        new DialoguePage("Bjorn le Veteran", "Je suis Bjorn, veteran de la garde des cimes. Vingt ans que je surveille ces montagnes, et jamais je n'ai vu autant d'ours aussi agressifs."),
                        new DialoguePage("Bjorn le Veteran", "J'en ai vu cinq roder pres du col principal. Ils attaquent les voyageurs sans provocation. Quelque chose les rend fous."),
                        new DialoguePage("Bjorn le Veteran", "T'as l'air costaud. Elimine ces cinq ours et je te recompenserai grassement.")
                ),
                null, null, 1440
        );
    }
}
