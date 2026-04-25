package com.eldanior.system.quest.definitions.journaliere.exploration;

import com.eldanior.system.quest.*;

public class DailyExplore_Medium extends QuestModel {
    public DailyExplore_Medium() {
        super("daily_explore_medium", "Grand Explorateur",
                "Decouvrez de nombreux coffres.",
                QuestType.EXPLORATION, QuestCategory.JOURNALIERE, QuestDifficulty.D,
                null, 8,
                665, 330, null,
                null, null, 1440);
    }
}
