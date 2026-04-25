package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_Tiny extends QuestModel {
    public DailyCollect_Tiny() {
        super("daily_collect_tiny", "Quelques Pieces",
                "Accumulez un peu d'or.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                null, 200,
                100, 50, null,
                null, null, 1440);
    }
}
