package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

public class DailyPK_Terreur1 extends QuestModel {
    public DailyPK_Terreur1() {
        super("daily_pk_terreur1", "Terreur Nocturne",
                "Repandez la destruction.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 50,
                1250, 750, null,
                null, null, 1440);
    }
}
