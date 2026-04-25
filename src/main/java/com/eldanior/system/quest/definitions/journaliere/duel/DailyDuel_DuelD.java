package com.eldanior.system.quest.definitions.journaliere.duel;

import com.eldanior.system.quest.*;

public class DailyDuel_DuelD extends QuestModel {
    public DailyDuel_DuelD() {
        super("daily_duel_duel_d", "Duelliste Aguerri",
                "Affirmez-vous en duel.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 2,
                1250, 750, null,
                null, null, 1440);
    }
}
