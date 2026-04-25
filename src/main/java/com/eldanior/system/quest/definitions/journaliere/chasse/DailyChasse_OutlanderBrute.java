package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_OutlanderBrute extends QuestModel {
    public DailyChasse_OutlanderBrute() {
        super("daily_chasse_outlander_brute", "Brutes des Terres Lointaines",
                "Eliminez des brutes outlanders.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.A,
                "outlander brute", 3,
                2285, 1715, null,
                null, null, 1440);
    }
}
