package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Duel4 extends QuestModel {
    public DailyPK_Duel4() {
        super("daily_pk_duel4", "Champion des Tenebres",
                "Regnez sur les duels.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.S,
                null, 5,
                3000, 2000, null,
                null, null, 1440);
    }
}
