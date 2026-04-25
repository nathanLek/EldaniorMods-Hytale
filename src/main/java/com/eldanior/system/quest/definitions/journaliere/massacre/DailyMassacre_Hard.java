package com.eldanior.system.quest.definitions.journaliere.massacre;

import com.eldanior.system.quest.*;

public class DailyMassacre_Hard extends QuestModel {
    public DailyMassacre_Hard() {
        super("daily_massacre_hard", "Exterminateur",
                "Eliminez tout sur votre passage.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 30,
                2000, 1200, null,
                null, null, 1440);
    }
}
