package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_VoidSpectre extends QuestModel {
    public DailyChasse_VoidSpectre() {
        super("daily_chasse_void_spectre", "Spectres du Neant",
                "Eliminez des spectres du vide.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.S,
                "spectre", 3,
                3000, 2000, null,
                null, null, 1440);
    }
}
