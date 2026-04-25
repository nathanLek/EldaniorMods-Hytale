package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_SkeletonSand extends QuestModel {
    public DailyChasse_SkeletonSand() {
        super("daily_chasse_skeleton_sand", "Sables Maudits",
                "Eliminez des squelettes de sable.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                "sand", 8,
                1250, 750, null,
                null, null, 1440);
    }
}
