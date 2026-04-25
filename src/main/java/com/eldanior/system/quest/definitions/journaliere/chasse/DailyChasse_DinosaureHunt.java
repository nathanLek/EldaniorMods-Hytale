package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_DinosaureHunt extends QuestModel {
    public DailyChasse_DinosaureHunt() {
        super("daily_chasse_dinosaure_hunt", "Chasseur Prehistorique",
                "Eliminez des dinosaures.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.S,
                "raptor", 3,
                3000, 2000, null,
                null, null, 1440);
    }
}
