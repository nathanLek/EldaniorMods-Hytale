package com.eldanior.system.quest.definitions.journaliere.massacre;

import com.eldanior.system.quest.*;

public class DailyMassacre_Exterminateur extends QuestModel {
    public DailyMassacre_Exterminateur() {
        super("daily_massacre_2", "Exterminateur",
                "Tuez 100 monstres.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 100,
                2000, 3000, null,
                null, null, 1440);
    }
}
