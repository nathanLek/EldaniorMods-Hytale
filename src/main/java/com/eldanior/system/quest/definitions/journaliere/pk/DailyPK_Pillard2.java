package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Pillard2 extends QuestModel {
    public DailyPK_Pillard2() {
        super("daily_pk_pillard2", "Butin de Guerre",
                "Amassez du butin.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 3000,
                2000, 1200, null,
                null, null, 1440);
    }
}
