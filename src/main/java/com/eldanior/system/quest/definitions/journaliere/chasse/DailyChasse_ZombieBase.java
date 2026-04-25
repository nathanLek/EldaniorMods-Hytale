package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_ZombieBase extends QuestModel {
    public DailyChasse_ZombieBase() {
        super("daily_chasse_zombie_base", "Chasseur de Zombies",
                "Eliminez des zombies.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                "zombie", 15,
                100, 50, null,
                null, null, 1440);
    }
}
