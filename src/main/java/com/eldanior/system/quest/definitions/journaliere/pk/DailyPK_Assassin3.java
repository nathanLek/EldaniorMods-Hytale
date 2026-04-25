package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Assassin3 extends QuestModel {
    public DailyPK_Assassin3() {
        super("daily_pk_assassin3", "Tueur en Serie",
                "Eliminez 5 joueurs.",
                QuestType.EXECUTION, QuestCategory.JOURNALIERE, QuestDifficulty.A,
                null, 5,
                2285, 1715, null,
                null, null, 1440);
    }

    @Override
    public String getObjectiveText() {
        return "Eliminer 5 joueurs";
    }
}
