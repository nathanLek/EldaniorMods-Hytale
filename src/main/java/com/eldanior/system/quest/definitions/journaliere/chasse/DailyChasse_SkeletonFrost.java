package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_SkeletonFrost extends QuestModel {
    public DailyChasse_SkeletonFrost() {
        super("daily_chasse_skeleton_frost", "Ossements Givres",
                "Eliminez des squelettes de givre.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.D,
                "frost", 10,
                665, 330, null,
                null, null, 1440);
    }
}
