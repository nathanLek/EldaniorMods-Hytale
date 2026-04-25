package com.eldanior.system.quest.definitions.journaliere.exploration;

import com.eldanior.system.quest.*;

public class DailyExplore_ChasseurCoffres extends QuestModel {
    public DailyExplore_ChasseurCoffres() {
        super("daily_explore_1", "Chasseur de Coffres",
                "Decouvrez 3 coffres.",
                QuestType.EXPLORATION, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                null, 3,
                100, 50, null,
                null, null, 1440);
    }
}
