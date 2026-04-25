package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Terreur2 extends QuestModel {
    public DailyPK_Terreur2() {
        super("daily_pk_terreur2", "Devastateur",
                "Devastez tout.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 30,
                2000, 1200, null,
                null, null, 1440);
    }
}
