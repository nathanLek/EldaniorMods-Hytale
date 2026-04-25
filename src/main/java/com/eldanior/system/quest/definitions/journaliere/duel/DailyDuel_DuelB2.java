package com.eldanior.system.quest.definitions.journaliere.duel;

import com.eldanior.system.quest.*;

public class DailyDuel_DuelB2 extends QuestModel {
    public DailyDuel_DuelB2() {
        super("daily_duel_duel_b2", "Combattant d'Elite",
                "Ecrasez vos adversaires.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 4,
                2000, 1200, null,
                null, null, 1440);
    }
}
