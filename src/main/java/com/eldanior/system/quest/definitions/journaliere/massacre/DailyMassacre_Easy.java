package com.eldanior.system.quest.definitions.journaliere.massacre;

import com.eldanior.system.quest.*;

public class DailyMassacre_Easy extends QuestModel {
    public DailyMassacre_Easy() {
        super("daily_massacre_easy", "Petit Carnage",
                "Tuez quelques monstres.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                null, 20,
                100, 50, null,
                null, null, 1440);
    }
}
