package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Assassin9 extends QuestModel {
    public DailyPK_Assassin9() {
        super("daily_pk_assassin9", "Massacre Sanglant",
                "Bain de sang.",
                QuestType.EXECUTION, QuestCategory.JOURNALIERE, QuestDifficulty.S,
                null, 10,
                3000, 2000, null,
                null, null, 1440);
    }

    @Override
    public String getObjectiveText() {
        return "Eliminer 10 joueurs";
    }
}
