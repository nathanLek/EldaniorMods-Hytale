package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_TrorkWarrior extends QuestModel {
    public DailyChasse_TrorkWarrior() {
        super("daily_chasse_trork_warrior", "Guerrier Trork",
                "Affrontez les guerriers trorks.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                "trork warrior", 6,
                2000, 1200, null,
                null, null, 1440);
    }
}
