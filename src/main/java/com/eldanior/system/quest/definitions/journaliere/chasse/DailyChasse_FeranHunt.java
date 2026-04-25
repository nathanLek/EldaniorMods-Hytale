package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_FeranHunt extends QuestModel {
    public DailyChasse_FeranHunt() {
        super("daily_chasse_feran_hunt", "Chasse aux Ferans",
                "Eliminez des ferans.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.E,
                "feran", 10,
                330, 165, null,
                null, null, 1440);
    }
}
