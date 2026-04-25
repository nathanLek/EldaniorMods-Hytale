package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_GoblinRaid extends QuestModel {
    public DailyChasse_GoblinRaid() {
        super("daily_chasse_goblin_raid", "Raid Gobelin",
                "Lancez un raid contre les gobelins.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.E,
                "goblin", 20,
                330, 165, null,
                null, null, 1440);
    }
}
