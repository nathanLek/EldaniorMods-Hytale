package com.eldanior.system.quest.definitions.journaliere.duel;

import com.eldanior.system.quest.*;

public class DailyDuel_Champion extends QuestModel {
    public DailyDuel_Champion() {
        super("daily_duel_champion", "Champion du Jour",
                "Dominez en duel.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.A,
                null, 5,
                2285, 1715, null,
                null, null, 1440);
    }
}
