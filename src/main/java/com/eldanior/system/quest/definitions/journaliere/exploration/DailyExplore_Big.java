package com.eldanior.system.quest.definitions.journaliere.exploration;

import com.eldanior.system.quest.*;

public class DailyExplore_Big extends QuestModel {
    public DailyExplore_Big() {
        super("daily_explore_big", "Aventurier Intrepide",
                "Explorez les recoins du monde.",
                QuestType.EXPLORATION, QuestCategory.JOURNALIERE, QuestDifficulty.D,
                null, 12,
                665, 330, null,
                null, null, 1440);
    }
}
