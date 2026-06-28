package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N8 extends NpcDialogueQuest {
    public SecQuest_Chasse_N8() {
        super(
                "sec_chasse_n8",
                "Purge des Profondeurs",
                "Eliminer les squelettes de sable qui infestent les profondeurs.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                "sand", 40,
                3000, 2500, null,
                null, "Quest_Npc_Secondaire_Chasse_N8",
                List.of(
                        new DialoguePage("Thorin le Nain", "Par ma barbe ! Thorin, mineur en chef de la guilde des profondeurs. On a un sacre probleme dans les tunnels du desert."),
                        new DialoguePage("Thorin le Nain", "Ils sortent des murs comme des cafards, ces squelettes de sable. Impossible de creuser sans se faire attaquer toutes les cinq minutes."),
                        new DialoguePage("Thorin le Nain", "Quarante de ces ossements ambulants a broyer, et on pourra reprendre le travail. La guilde paiera bien.")
                ),
                null, null, 1440
        );
    }
}
