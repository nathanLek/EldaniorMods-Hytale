package com.eldanior.system.quest.definitions.journaliere.collection;

import com.eldanior.system.quest.*;

public class DailyCollect_CollectC2 extends QuestModel {
    public DailyCollect_CollectC2() {
        super("daily_collect_collect_c2", "Investisseur Avise",
                "Versez votre or à la banque : le banquier récompense ceux qui épargnent (un retrait fait reculer la quête).",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 7000,
                1250, 750, null,
                null, null, 1440);
    }
}
