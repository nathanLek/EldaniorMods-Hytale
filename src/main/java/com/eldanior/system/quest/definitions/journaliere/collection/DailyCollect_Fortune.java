package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_Fortune extends QuestModel {
    public DailyCollect_Fortune() {
        super("daily_collect_2", "Fortune",
                "Accumulez 5000 Or.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 5000,
                1250, 750, null,
                null, null, 1440);
    }
}