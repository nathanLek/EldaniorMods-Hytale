package com.eldanior.system.quest.definitions.journaliere.exploration;

import com.eldanior.system.quest.*;

public class DailyExplore_GrandExplorateur extends QuestModel {
    public DailyExplore_GrandExplorateur() {
        super("daily_explore_2", "Grand Explorateur",
                "Decouvrez 10 coffres.",
                QuestType.EXPLORATION, QuestCategory.JOURNALIERE, QuestDifficulty.D,
                null, 10,
                1000, 2000, null,
                null, null, 1440);
    }
}
