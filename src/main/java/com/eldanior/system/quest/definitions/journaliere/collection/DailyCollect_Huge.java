package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_Huge extends QuestModel {
    public DailyCollect_Huge() {
        super("daily_collect_huge", "Coffre de Guerre",
                "Remplissez votre coffre.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 10000,
                2000, 1200, null,
                null, null, 1440);
    }
}
