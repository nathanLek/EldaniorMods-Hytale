package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_CollectS extends QuestModel {
    public DailyCollect_CollectS() {
        super("daily_collect_collect_s", "Richesse Supreme",
                "Accumulez une fortune legendaire.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.S,
                null, 50000,
                3000, 2000, null,
                null, null, 1440);
    }
}
