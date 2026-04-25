package com.eldanior.system.quest.definitions.journaliere.exploration;

import com.eldanior.system.quest.*;

public class DailyExplore_ExploreE extends QuestModel {
    public DailyExplore_ExploreE() {
        super("daily_explore_explore_e", "Chercheur de Tresors",
                "Trouvez des tresors caches.",
                QuestType.EXPLORATION, QuestCategory.JOURNALIERE, QuestDifficulty.E,
                null, 6,
                330, 165, null,
                null, null, 1440);
    }
}
