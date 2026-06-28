package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N20 extends NpcDialogueQuest {
    public SecQuest_Chasse_N20() {
        super(
                "sec_chasse_n20",
                "L'Extinction Finale",
                "Accomplir l'ultime purge en eliminant deux cents creatures.",
                QuestType.MASSACRE, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                null, 200,
                10000, 8000, "extinction",
                null, "Quest_Npc_Secondaire_Chasse_N20",
                List.of(
                        new DialoguePage("Balthazar l'Ancien", "Je suis Balthazar, ancien maitre de guerre. J'ai vecu trois siecles grace a la magie, mais mon temps touche a sa fin."),
                        new DialoguePage("Balthazar l'Ancien", "Mon dernier souhait est de voir cette terre purifiee avant de partir. Deux cents creatures doivent tomber pour que l'equilibre soit restaure."),
                        new DialoguePage("Balthazar l'Ancien", "Si tu y parviens, je te leguerai tout ce que je possede. Mon or, mon savoir, et le titre d'Extinction que j'ai porte autrefois.")
                ),
                null, null, 1440
        );
    }
}
