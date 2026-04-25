package com.eldanior.system.quest.definitions.journaliere.duel;

import com.eldanior.system.quest.*;

public class DailyDuel_DuelF extends QuestModel {
    public DailyDuel_DuelF() {
        super("daily_duel_duel_f", "Debutant en Duel",
                "Tentez votre chance en duel.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                null, 1,
                100, 50, null,
                null, null, 1440);
    }
}
