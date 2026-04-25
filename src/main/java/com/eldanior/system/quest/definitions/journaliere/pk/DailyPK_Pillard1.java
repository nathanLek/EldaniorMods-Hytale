package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Pillard1 extends QuestModel {
    public DailyPK_Pillard1() {
        super("daily_pk_pillard1", "Pillard Infame",
                "Accumulez de l'or.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 8000,
                1250, 750, null,
                null, null, 1440);
    }
}
