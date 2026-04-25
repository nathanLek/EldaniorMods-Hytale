package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Assassin5 extends QuestModel {
    public DailyPK_Assassin5() {
        super("daily_pk_assassin5", "Ange de la Mort",
                "Semez la terreur.",
                QuestType.EXECUTION, QuestCategory.JOURNALIERE, QuestDifficulty.A,
                null, 7,
                2285, 1715, null,
                null, null, 1440);
    }

    @Override
    public String getObjectiveText() {
        return "Eliminer 7 joueurs";
    }
}
