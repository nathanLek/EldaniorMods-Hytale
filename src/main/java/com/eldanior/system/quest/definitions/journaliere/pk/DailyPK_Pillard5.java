package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Pillard5 extends QuestModel {
    public DailyPK_Pillard5() {
        super("daily_pk_pillard5", "Roi des Voleurs",
                "Devenez le roi des voleurs.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.S,
                null, 25000,
                3000, 2000, null,
                null, null, 1440);
    }
}
