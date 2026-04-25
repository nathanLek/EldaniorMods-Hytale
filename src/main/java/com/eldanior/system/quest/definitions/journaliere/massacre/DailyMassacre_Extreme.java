package com.eldanior.system.quest.definitions.journaliere.massacre;

import com.eldanior.system.quest.*;

public class DailyMassacre_Extreme extends QuestModel {
    public DailyMassacre_Extreme() {
        super("daily_massacre_extreme", "Apocalypse",
                "Semez la destruction totale.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 60,
                2000, 1200, null,
                null, null, 1440);
    }
}
