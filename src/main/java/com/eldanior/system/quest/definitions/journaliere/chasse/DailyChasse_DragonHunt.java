package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_DragonHunt extends QuestModel {
    public DailyChasse_DragonHunt() {
        super("daily_chasse_dragon_hunt", "Chasse au Dragon",
                "Affrontez un dragon.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.S,
                "dragon", 1,
                3000, 2000, null,
                null, null, 1440);
    }
}
