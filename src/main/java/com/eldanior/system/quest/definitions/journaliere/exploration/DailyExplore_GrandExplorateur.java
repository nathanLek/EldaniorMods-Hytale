package com.eldanior.system.quest.definitions.journaliere.exploration;

import com.eldanior.system.quest.*;

public class DailyExplore_GrandExplorateur extends QuestModel {
    public DailyExplore_GrandExplorateur() {
        super("daily_explore_2", "Grand Explorateur",
                "Decouvrez 8 coffres.",
                QuestType.EXPLORATION, QuestCategory.JOURNALIERE, QuestDifficulty.D,
                null, 8,
                665, 330, null,
                null, null, 1440);
    }
}
