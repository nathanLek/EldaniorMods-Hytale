package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Duel2 extends QuestModel {
    public DailyPK_Duel2() {
        super("daily_pk_duel2", "Duel de l'Ombre",
                "Combattez dans l'ombre.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 1,
                2000, 1200, null,
                null, null, 1440);
    }
}
