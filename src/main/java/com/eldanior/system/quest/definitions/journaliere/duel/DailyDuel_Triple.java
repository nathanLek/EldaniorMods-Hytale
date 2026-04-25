package com.eldanior.system.quest.definitions.journaliere.duel;

import com.eldanior.system.quest.*;

public class DailyDuel_Triple extends QuestModel {
    public DailyDuel_Triple() {
        super("daily_duel_triple", "Triple Menace",
                "Gagnez 3 duels.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 3,
                2000, 1200, null,
                null, null, 1440);
    }
}
