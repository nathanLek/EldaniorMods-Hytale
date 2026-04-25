package com.eldanior.system.quest.definitions.journaliere.duel;

import com.eldanior.system.quest.*;

public class DailyDuel_Invincible extends QuestModel {
    public DailyDuel_Invincible() {
        super("daily_duel_invincible", "Invincible",
                "Gagnez sans relache.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.S,
                null, 7,
                3000, 2000, null,
                null, null, 1440);
    }
}
