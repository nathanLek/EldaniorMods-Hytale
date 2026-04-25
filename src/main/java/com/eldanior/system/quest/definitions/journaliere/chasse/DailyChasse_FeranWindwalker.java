package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_FeranWindwalker extends QuestModel {
    public DailyChasse_FeranWindwalker() {
        super("daily_chasse_feran_windwalker", "Traque du Vent",
                "Eliminez des ferans avances.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.D,
                "feran", 15,
                665, 330, null,
                null, null, 1440);
    }
}
