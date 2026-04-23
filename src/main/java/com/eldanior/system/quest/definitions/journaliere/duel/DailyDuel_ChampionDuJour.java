package com.eldanior.system.quest.definitions.journaliere.duel;

import com.eldanior.system.quest.*;

public class DailyDuel_ChampionDuJour extends QuestModel {
    public DailyDuel_ChampionDuJour() {
        super("daily_duel_2", "Champion du Jour",
                "Gagnez 5 duels.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 5,
                2000, 5000, null,
                null, null, 1440);
    }
}