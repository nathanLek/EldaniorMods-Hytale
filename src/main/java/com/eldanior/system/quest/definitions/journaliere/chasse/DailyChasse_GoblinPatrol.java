package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_GoblinPatrol extends QuestModel {
    public DailyChasse_GoblinPatrol() {
        super("daily_chasse_goblin_patrol", "Patrouille Gobeline",
                "Eliminez des patrouilles gobelines.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.E,
                "goblin", 12,
                330, 165, null,
                null, null, 1440);
    }
}
