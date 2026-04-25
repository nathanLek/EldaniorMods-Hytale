package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Assassin6 extends QuestModel {
    public DailyPK_Assassin6() {
        super("daily_pk_assassin6", "Executeur",
                "Executez vos ennemis.",
                QuestType.EXECUTION, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 3,
                1250, 750, null,
                null, null, 1440);
    }

    @Override
    public String getObjectiveText() {
        return "Eliminer 3 joueurs";
    }
}
