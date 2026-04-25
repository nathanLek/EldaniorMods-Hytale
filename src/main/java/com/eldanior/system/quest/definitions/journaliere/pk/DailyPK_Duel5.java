package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Duel5 extends QuestModel {
    public DailyPK_Duel5() {
        super("daily_pk_duel5", "Duel Sanglant",
                "Combattez jusqu'au sang.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 2,
                2000, 1200, null,
                null, null, 1440);
    }
}
