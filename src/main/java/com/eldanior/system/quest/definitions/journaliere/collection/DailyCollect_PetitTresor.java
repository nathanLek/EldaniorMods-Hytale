package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_PetitTresor extends QuestModel {
    public DailyCollect_PetitTresor() {
        super("daily_collect_1", "Petit Tresor",
                "Accumulez 500 Or.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.E,
                null, 500,
                330, 165, null,
                null, null, 1440);
    }
}
