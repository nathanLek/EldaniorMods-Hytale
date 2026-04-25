package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Assassin4 extends QuestModel {
    public DailyPK_Assassin4() {
        super("daily_pk_assassin4", "Chasseur de Primes",
                "Eliminez des cibles.",
                QuestType.EXECUTION, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 4,
                2000, 1200, null,
                null, null, 1440);
    }

    @Override
    public String getObjectiveText() {
        return "Eliminer 4 joueurs";
    }
}
