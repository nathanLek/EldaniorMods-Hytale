package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Assassin8 extends QuestModel {
    public DailyPK_Assassin8() {
        super("daily_pk_assassin8", "Lame Silencieuse",
                "Frappez dans l'ombre.",
                QuestType.EXECUTION, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 2,
                2000, 1200, null,
                null, null, 1440);
    }

    @Override
    public String getObjectiveText() {
        return "Eliminer 2 joueurs";
    }
}
