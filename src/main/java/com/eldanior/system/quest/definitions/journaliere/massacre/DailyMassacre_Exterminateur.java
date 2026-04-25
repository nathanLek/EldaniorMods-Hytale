package com.eldanior.system.quest.definitions.journaliere.massacre;

import com.eldanior.system.quest.*;

public class DailyMassacre_Exterminateur extends QuestModel {
    public DailyMassacre_Exterminateur() {
        super("daily_massacre_2", "Exterminateur",
                "Tuez 30 monstres.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 30,
                2000, 1200, null,
                null, null, 1440);
    }
}
