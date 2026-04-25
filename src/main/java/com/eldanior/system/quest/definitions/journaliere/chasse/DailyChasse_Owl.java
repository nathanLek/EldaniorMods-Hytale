package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_Owl extends QuestModel {
    public DailyChasse_Owl() {
        super("daily_chasse_owl", "Chasse Nocturne",
                "Chassez des hiboux.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                "owl", 8,
                100, 50, null,
                null, null, 1440);
    }
}
