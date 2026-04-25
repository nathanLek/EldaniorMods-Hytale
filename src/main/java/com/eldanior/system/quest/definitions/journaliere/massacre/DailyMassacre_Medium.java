package com.eldanior.system.quest.definitions.journaliere.massacre;

import com.eldanior.system.quest.*;

public class DailyMassacre_Medium extends QuestModel {
    public DailyMassacre_Medium() {
        super("daily_massacre_medium", "Carnage",
                "Repandez la destruction.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.D,
                null, 40,
                665, 330, null,
                null, null, 1440);
    }
}
