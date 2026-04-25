package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Assassin7 extends QuestModel {
    public DailyPK_Assassin7() {
        super("daily_pk_assassin7", "Predateur",
                "Traquez vos proies.",
                QuestType.EXECUTION, QuestCategory.JOURNALIERE, QuestDifficulty.A,
                null, 6,
                2285, 1715, null,
                null, null, 1440);
    }

    @Override
    public String getObjectiveText() {
        return "Eliminer 6 joueurs";
    }
}
