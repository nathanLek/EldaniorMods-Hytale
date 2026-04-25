package com.eldanior.system.quest.definitions.journaliere.exploration;

import com.eldanior.system.quest.*;

public class DailyExplore_ExploreB2 extends QuestModel {
    public DailyExplore_ExploreB2() {
        super("daily_explore_explore_b2", "Cartographe",
                "Cartographiez le monde.",
                QuestType.EXPLORATION, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 18,
                2000, 1200, null,
                null, null, 1440);
    }
}
