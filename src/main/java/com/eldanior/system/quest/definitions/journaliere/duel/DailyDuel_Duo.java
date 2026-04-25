package com.eldanior.system.quest.definitions.journaliere.duel;

import com.eldanior.system.quest.*;

public class DailyDuel_Duo extends QuestModel {
    public DailyDuel_Duo() {
        super("daily_duel_duo", "Double Victoire",
                "Gagnez 2 duels.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.D,
                null, 2,
                665, 330, null,
                null, null, 1440);
    }
}
