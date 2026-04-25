package com.eldanior.system.quest.definitions.journaliere.massacre;

import com.eldanior.system.quest.*;

public class DailyMassacre_MassacreS extends QuestModel {
    public DailyMassacre_MassacreS() {
        super("daily_massacre_massacre_s", "Armageddon",
                "Detruisez tout ce qui bouge.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.S,
                null, 15,
                3000, 2000, null,
                null, null, 1440);
    }
}
