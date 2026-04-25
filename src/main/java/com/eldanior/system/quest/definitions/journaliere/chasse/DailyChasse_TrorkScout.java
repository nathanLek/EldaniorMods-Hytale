package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_TrorkScout extends QuestModel {
    public DailyChasse_TrorkScout() {
        super("daily_chasse_trork_scout", "Eclaireur Trork",
                "Eliminez des trorks.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                "trork", 10,
                2000, 1200, null,
                null, null, 1440);
    }
}
