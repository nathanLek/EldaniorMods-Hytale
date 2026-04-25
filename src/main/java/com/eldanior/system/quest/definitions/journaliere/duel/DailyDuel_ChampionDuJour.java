package com.eldanior.system.quest.definitions.journaliere.duel;

import com.eldanior.system.quest.*;

public class DailyDuel_ChampionDuJour extends QuestModel {
    public DailyDuel_ChampionDuJour() {
        super("daily_duel_2", "Champion du Jour",
                "Gagnez 3 duels.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 3,
                2000, 1200, null,
                null, null, 1440);
    }
}