package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_SpiritHunt extends QuestModel {
    public DailyChasse_SpiritHunt() {
        super("daily_chasse_spirit_hunt", "Chasse aux Esprits",
                "Eliminez des esprits elementaires.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.A,
                "spirit", 4,
                2285, 1715, null,
                null, null, 1440);
    }
}
