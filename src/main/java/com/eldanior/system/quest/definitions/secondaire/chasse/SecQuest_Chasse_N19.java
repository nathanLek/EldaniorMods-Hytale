package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N19 extends NpcDialogueQuest {
    public SecQuest_Chasse_N19() {
        super(
                "sec_chasse_n19",
                "Duel de Lezards",
                "Eliminer les Saurians qui tendent des embuscades.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                "saurian", 20,
                1600, 1100, null,
                null, "Quest_Npc_Secondaire_Chasse_N19",
                List.of(
                        new DialoguePage("Zara la Lame", "Zara. Specialiste des Saurians depuis dix ans. Ces lezards sont plus malins qu'ils en ont l'air, crois-moi."),
                        new DialoguePage("Zara la Lame", "Ils organisent des embuscades coordonnees sur la route du marais. Trois groupes, positions strategiques, encerclement parfait. C'est presque militaire."),
                        new DialoguePage("Zara la Lame", "C'est un travail de professionnel qu'il faut pour les deloger. Vingt Saurians a abattre. Tu es a la hauteur ?")
                ),
                null, null, 1440
        );
    }
}
