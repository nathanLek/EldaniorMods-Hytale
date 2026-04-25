package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Assassin2 extends QuestModel {
    public DailyPK_Assassin2() {
        super("daily_pk_assassin2", "Double Lame",
                "Tuez 2 joueurs rapidement.",
                QuestType.EXECUTION, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 2,
                1250, 750, null,
                null, null, 1440);
    }

    @Override
    public String getObjectiveText() {
        return "Eliminer 2 joueurs";
    }
}
