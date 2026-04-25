package com.eldanior.system.quest.definitions.journaliere.massacre;

import com.eldanior.system.quest.*;

public class DailyMassacre_Carnage extends QuestModel {
    public DailyMassacre_Carnage() {
        super("daily_massacre_1", "Carnage",
                "Tuez 40 monstres.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.D,
                null, 40,
                665, 330, null,
                null, null, 1440);
    }
}
