package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_CollectF2 extends QuestModel {
    public DailyCollect_CollectF2() {
        super("daily_collect_collect_f2", "Mendiant Chanceux",
                "Ramassez quelques pieces.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                null, 100,
                100, 50, null,
                null, null, 1440);
    }
}
