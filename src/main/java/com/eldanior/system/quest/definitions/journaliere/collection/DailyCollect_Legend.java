package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_Legend extends QuestModel {
    public DailyCollect_Legend() {
        super("daily_collect_legend", "Tresor de Roi",
                "Accumulez un tresor royal.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.A,
                null, 20000,
                2285, 1715, null,
                null, null, 1440);
    }
}
