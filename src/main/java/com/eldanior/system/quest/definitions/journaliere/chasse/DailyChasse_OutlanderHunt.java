package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_OutlanderHunt extends QuestModel {
    public DailyChasse_OutlanderHunt() {
        super("daily_chasse_outlander_hunt", "Chasse aux Outlanders",
                "Eliminez des outlanders.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                "outlander", 8,
                2000, 1200, null,
                null, null, 1440);
    }
}
