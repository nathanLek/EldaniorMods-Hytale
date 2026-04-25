package com.eldanior.system.quest.definitions.journaliere.exploration;

import com.eldanior.system.quest.*;

public class DailyExplore_Tiny extends QuestModel {
    public DailyExplore_Tiny() {
        super("daily_explore_tiny", "Petit Explorateur",
                "Decouvrez quelques coffres.",
                QuestType.EXPLORATION, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                null, 3,
                100, 50, null,
                null, null, 1440);
    }
}
