package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_ScarakHunt extends QuestModel {
    public DailyChasse_ScarakHunt() {
        super("daily_chasse_scarak_hunt", "Extermination Scarak",
                "Eliminez des scaraks.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.S,
                "scarak", 5,
                3000, 2000, null,
                null, null, 1440);
    }
}
