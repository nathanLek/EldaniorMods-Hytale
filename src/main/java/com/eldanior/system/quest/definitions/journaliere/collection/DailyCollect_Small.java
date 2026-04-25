package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_Small extends QuestModel {
    public DailyCollect_Small() {
        super("daily_collect_small", "Petit Tresor",
                "Amassez un petit tresor.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.E,
                null, 500,
                330, 165, null,
                null, null, 1440);
    }
}
