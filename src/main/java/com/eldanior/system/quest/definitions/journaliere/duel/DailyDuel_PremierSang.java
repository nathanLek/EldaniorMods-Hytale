package com.eldanior.system.quest.definitions.journaliere.duel;

import com.eldanior.system.quest.*;

public class DailyDuel_PremierSang extends QuestModel {
    public DailyDuel_PremierSang() {
        super("daily_duel_1", "Premier Sang",
                "Gagnez un duel.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                null, 1,
                500, 1000, null,
                null, null, 1440);
    }
}