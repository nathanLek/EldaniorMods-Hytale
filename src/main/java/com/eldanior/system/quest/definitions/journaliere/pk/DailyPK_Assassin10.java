package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Assassin10 extends QuestModel {
    public DailyPK_Assassin10() {
        super("daily_pk_assassin10", "Premier Crime",
                "Commettez votre premier meurtre.",
                QuestType.EXECUTION, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 1,
                1250, 750, null,
                null, null, 1440);
    }

    @Override
    public String getObjectiveText() {
        return "Eliminer 1 joueur";
    }
}
