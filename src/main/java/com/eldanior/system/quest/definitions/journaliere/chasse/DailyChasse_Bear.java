package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_Bear extends QuestModel {
    public DailyChasse_Bear() {
        super("daily_chasse_bear", "Chasse a l'Ours",
                "Eliminez des ours.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                "bear", 5,
                100, 50, null,
                null, null, 1440);
    }
}
