package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_GoblinHermit extends QuestModel {
    public DailyChasse_GoblinHermit() {
        super("daily_chasse_goblin_hermit", "L'Ermite Gobelin",
                "Traquez les ermites gobelins.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.D,
                "goblin hermit", 5,
                665, 330, null,
                null, null, 1440);
    }
}
