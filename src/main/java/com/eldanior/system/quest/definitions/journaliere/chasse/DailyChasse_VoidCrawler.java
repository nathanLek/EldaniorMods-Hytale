package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_VoidCrawler extends QuestModel {
    public DailyChasse_VoidCrawler() {
        super("daily_chasse_void_crawler", "Rampants du Vide",
                "Eliminez des crawlers du vide.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.A,
                "crawler", 6,
                2285, 1715, null,
                null, null, 1440);
    }
}
