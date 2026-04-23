package com.eldanior.system.quest.definitions.secondaire;

import com.eldanior.system.quest.*;

public class SideQuest_Explorateur extends QuestModel {
    public SideQuest_Explorateur() {
        super("side_explore_1", "Explorateur",
                "Partez a la decouverte de coffres caches.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.F,
                null, 5,
                1000, 2000, null,
                null, null);
    }
}