package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N1 extends NpcDialogueQuest {
    public SecQuest_Chasse_N1() {
        super(
                "sec_chasse_n1",
                "Menace Lupine",
                "Eliminer les loups qui menacent le village.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                "wolf", 10,
                500, 300, null,
                null, "Quest_Npc_Secondaire_Chasse_N1",
                List.of(
                        new DialoguePage("Rodrik le Garde", "Halte, aventurier ! Je suis Rodrik, garde de ce village. Depuis quelques semaines, des meutes de loups descendent des collines et attaquent nos troupeaux."),
                        new DialoguePage("Rodrik le Garde", "Les fermiers n'osent plus sortir la nuit. Si tu pouvais eliminer une dizaine de ces betes, on pourrait enfin dormir tranquille."),
                        new DialoguePage("Rodrik le Garde", "Je te paierai bien sur pour ce service. Qu'en dis-tu ?")
                ),
                null, null, 1440
        );
    }
}
