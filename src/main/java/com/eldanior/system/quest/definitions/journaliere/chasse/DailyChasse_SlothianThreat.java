package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_SlothianThreat extends QuestModel {
    public DailyChasse_SlothianThreat() {
        super("daily_chasse_slothian_threat", "Menace Slothiane",
                "Repoussez les slothians.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.E,
                "slothian", 12,
                330, 165, null,
                null, null, 1440);
    }
}
