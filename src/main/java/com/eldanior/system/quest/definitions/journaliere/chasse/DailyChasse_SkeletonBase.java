package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_SkeletonBase extends QuestModel {
    public DailyChasse_SkeletonBase() {
        super("daily_chasse_skeleton_base", "Chasseur de Squelettes",
                "Eliminez des squelettes.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.F,
                "skeleton", 15,
                100, 50, null,
                null, null, 1440);
    }
}
