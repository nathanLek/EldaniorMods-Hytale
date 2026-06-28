package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N10 extends NpcDialogueQuest {
    public SecQuest_Chasse_N10() {
        super(
                "sec_chasse_n10",
                "Contrat Sanglant",
                "Eliminer cinquante creatures pour honorer le contrat.",
                QuestType.MASSACRE, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                null, 50,
                2000, 1500, null,
                null, "Quest_Npc_Secondaire_Chasse_N10",
                List.of(
                        new DialoguePage("Gareth le Mercenaire", "Gareth. Mercenaire. Pas besoin de presentations plus longues. J'ai un contrat et pas assez de bras pour le remplir."),
                        new DialoguePage("Gareth le Mercenaire", "Cinquante cibles, n'importe quel type de creature. Le commanditaire se fiche des details, il veut juste que la zone soit nettoyee."),
                        new DialoguePage("Gareth le Mercenaire", "On partage la prime. Cinquante-cinquante. C'est a prendre ou a laisser.")
                ),
                null, null, 1440
        );
    }
}
