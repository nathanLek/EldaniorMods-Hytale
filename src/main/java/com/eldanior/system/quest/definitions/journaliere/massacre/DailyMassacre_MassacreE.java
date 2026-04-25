package com.eldanior.system.quest.definitions.journaliere.massacre;

import com.eldanior.system.quest.*;

public class DailyMassacre_MassacreE extends QuestModel {
    public DailyMassacre_MassacreE() {
        super("daily_massacre_massacre_e", "Purge Modeste",
                "Purgez les creatures hostiles.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.E,
                null, 30,
                330, 165, null,
                null, null, 1440);
    }
}
