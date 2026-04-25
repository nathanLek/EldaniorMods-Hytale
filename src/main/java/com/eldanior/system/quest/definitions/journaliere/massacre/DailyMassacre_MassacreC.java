package com.eldanior.system.quest.definitions.journaliere.massacre;

import com.eldanior.system.quest.*;

public class DailyMassacre_MassacreC extends QuestModel {
    public DailyMassacre_MassacreC() {
        super("daily_massacre_massacre_c", "Bain de Sang",
                "Repandez le sang ennemi.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 35,
                1250, 750, null,
                null, null, 1440);
    }
}
