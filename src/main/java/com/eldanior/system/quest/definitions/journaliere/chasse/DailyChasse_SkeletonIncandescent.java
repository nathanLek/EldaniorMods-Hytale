package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_SkeletonIncandescent extends QuestModel {
    public DailyChasse_SkeletonIncandescent() {
        super("daily_chasse_skeleton_incandescent", "Flammes Eternelles",
                "Eliminez des squelettes incandescents.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                "incandescent", 6,
                1250, 750, null,
                null, null, 1440);
    }
}
