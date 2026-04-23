package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_PetitTresor extends QuestModel {
    public DailyCollect_PetitTresor() {
        super("daily_collect_1", "Petit Tresor",
                "Accumulez 5000 Or.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                null, 5000,
                300, 0, null,
                null, null, 1440);
    }
}
