package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_Squelettes extends QuestModel {
    public DailyChasse_Squelettes() {
        super("daily_chasse_1", "Chasse aux Squelettes",
                "Eliminez des squelettes.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                "skeleton", 15,
                100, 50, null,
                null, null, 1440);
    }
}
