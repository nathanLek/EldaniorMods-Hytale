package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_Big extends QuestModel {
    public DailyCollect_Big() {
        super("daily_collect_big", "Grande Fortune",
                "Amassez une grande fortune.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 5000,
                1250, 750, null,
                null, null, 1440);
    }
}
