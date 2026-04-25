package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_Zombies extends QuestModel {
    public DailyChasse_Zombies() {
        super("daily_chasse_2", "Chasse aux Zombies",
                "Eliminez des zombies.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                "zombie", 15,
                100, 50, null,
                null, null, 1440);
    }
}
