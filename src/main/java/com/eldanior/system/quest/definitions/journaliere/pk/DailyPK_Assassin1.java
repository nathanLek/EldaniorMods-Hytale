package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Assassin1 extends QuestModel {
    public DailyPK_Assassin1() {
        super("daily_pk_assassin1", "Assassin du Jour",
                "Eliminez des joueurs.",
                QuestType.EXECUTION, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 3,
                2000, 1200, null,
                null, null, 1440);
    }

    @Override
    public String getObjectiveText() {
        return "Eliminer 3 joueurs";
    }
}
