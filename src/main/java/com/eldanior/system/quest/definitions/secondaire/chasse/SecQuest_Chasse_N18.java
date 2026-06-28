package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N18 extends NpcDialogueQuest {
    public SecQuest_Chasse_N18() {
        super(
                "sec_chasse_n18",
                "Chouettes Nocturnes",
                "Eliminer les chouettes agressives qui attaquent la nuit.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                "owl", 8,
                400, 250, null,
                null, "Quest_Npc_Secondaire_Chasse_N18",
                List.of(
                        new DialoguePage("Hugo le Trappeur", "Hugo, trappeur de la foret de l'ouest. J'ai un probleme avec des chouettes. Oui, des chouettes. Ris pas."),
                        new DialoguePage("Hugo le Trappeur", "J'ai des griffures partout. Ces bestioles m'attaquent des que la nuit tombe. Elles protegent leur territoire et mes pieges sont en plein dedans."),
                        new DialoguePage("Hugo le Trappeur", "Aide-moi et je te donnerai une partie de mes prises. Huit chouettes, c'est tout ce que je demande.")
                ),
                null, null, 1440
        );
    }
}
