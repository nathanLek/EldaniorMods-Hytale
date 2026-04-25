package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_VoidNecro extends QuestModel {
    public DailyChasse_VoidNecro() {
        super("daily_chasse_void_necro", "Necromanciens du Vide",
                "Eliminez des necromanciens.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.A,
                "necromancer", 4,
                2285, 1715, null,
                null, null, 1440);
    }
}
