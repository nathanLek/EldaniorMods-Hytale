package com.eldanior.system.quest.definitions.journaliere.duel;

import com.eldanior.system.quest.*;

public class DailyDuel_First extends QuestModel {
    public DailyDuel_First() {
        super("daily_duel_first", "Premier Sang",
                "Gagnez un duel.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.E,
                null, 1,
                330, 165, null,
                null, null, 1440);
    }
}
