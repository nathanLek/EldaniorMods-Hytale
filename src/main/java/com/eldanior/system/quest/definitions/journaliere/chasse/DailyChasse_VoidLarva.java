package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_VoidLarva extends QuestModel {
    public DailyChasse_VoidLarva() {
        super("daily_chasse_void_larva", "Larves du Vide",
                "Eliminez des larves du vide.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.A,
                "larva", 8,
                2285, 1715, null,
                null, null, 1440);
    }
}
