package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_Medium extends QuestModel {
    public DailyCollect_Medium() {
        super("daily_collect_medium", "Fortune Modeste",
                "Constituez une petite fortune.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.D,
                null, 2000,
                665, 330, null,
                null, null, 1440);
    }
}
