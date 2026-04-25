package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_Pigeon extends QuestModel {
    public DailyChasse_Pigeon() {
        super("daily_chasse_pigeon", "Chasse aux Pigeons",
                "Chassez des pigeons.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                "pigeon", 10,
                100, 50, null,
                null, null, 1440);
    }
}
