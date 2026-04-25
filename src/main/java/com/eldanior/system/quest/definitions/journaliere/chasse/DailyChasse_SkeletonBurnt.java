package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_SkeletonBurnt extends QuestModel {
    public DailyChasse_SkeletonBurnt() {
        super("daily_chasse_skeleton_burnt", "Cendres et Os",
                "Eliminez des squelettes brules.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                "burnt", 10,
                1250, 750, null,
                null, null, 1440);
    }
}
