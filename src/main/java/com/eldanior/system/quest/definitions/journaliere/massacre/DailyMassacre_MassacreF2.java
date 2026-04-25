package com.eldanior.system.quest.definitions.journaliere.massacre;

import com.eldanior.system.quest.*;

public class DailyMassacre_MassacreF2 extends QuestModel {
    public DailyMassacre_MassacreF2() {
        super("daily_massacre_massacre_f2", "Nettoyage de Zone",
                "Nettoyez la zone de monstres.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                null, 25,
                100, 50, null,
                null, null, 1440);
    }
}
