package com.eldanior.system.quest.definitions.journaliere.exploration;

import com.eldanior.system.quest.*;

public class DailyExplore_Legend extends QuestModel {
    public DailyExplore_Legend() {
        super("daily_explore_legend", "Legendaire Decouvreur",
                "Rien ne vous echappe.",
                QuestType.EXPLORATION, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 20,
                2000, 1200, null,
                null, null, 1440);
    }
}
