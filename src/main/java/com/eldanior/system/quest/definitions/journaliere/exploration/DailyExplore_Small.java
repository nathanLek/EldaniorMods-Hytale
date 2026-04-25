package com.eldanior.system.quest.definitions.journaliere.exploration;

import com.eldanior.system.quest.*;

public class DailyExplore_Small extends QuestModel {
    public DailyExplore_Small() {
        super("daily_explore_small", "Explorateur",
                "Partez a l'aventure.",
                QuestType.EXPLORATION, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                null, 5,
                100, 50, null,
                null, null, 1440);
    }
}
