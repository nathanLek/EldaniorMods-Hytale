package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_Fortune extends QuestModel {
    public DailyCollect_Fortune() {
        super("daily_collect_2", "Fortune",
                "Accumulez 50000 Or.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 50000,
                1500, 0, null,
                null, null, 1440);
    }
}
