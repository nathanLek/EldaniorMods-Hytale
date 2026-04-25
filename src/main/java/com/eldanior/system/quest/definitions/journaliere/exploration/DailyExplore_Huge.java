package com.eldanior.system.quest.definitions.journaliere.exploration;

import com.eldanior.system.quest.*;

public class DailyExplore_Huge extends QuestModel {
    public DailyExplore_Huge() {
        super("daily_explore_huge", "Maitre Explorateur",
                "Decouvrez tous les secrets.",
                QuestType.EXPLORATION, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 15,
                1250, 750, null,
                null, null, 1440);
    }
}
