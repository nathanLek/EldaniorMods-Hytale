package com.eldanior.system.quest.definitions.journaliere.massacre;

import com.eldanior.system.quest.*;

public class DailyMassacre_Legend extends QuestModel {
    public DailyMassacre_Legend() {
        super("daily_massacre_legend", "Legende de Guerre",
                "Prouvez votre valeur au combat.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.A,
                null, 25,
                2285, 1715, null,
                null, null, 1440);
    }
}
