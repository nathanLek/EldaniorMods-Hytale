package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_SaurianHunt extends QuestModel {
    public DailyChasse_SaurianHunt() {
        super("daily_chasse_saurian_hunt", "Chasse aux Sauriens",
                "Eliminez des sauriens.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.D,
                "saurian", 10,
                665, 330, null,
                null, null, 1440);
    }
}
