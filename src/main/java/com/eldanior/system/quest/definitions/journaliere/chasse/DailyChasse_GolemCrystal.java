package com.eldanior.system.quest.definitions.journaliere.chasse;

import com.eldanior.system.quest.*;

public class DailyChasse_GolemCrystal extends QuestModel {
    public DailyChasse_GolemCrystal() {
        super("daily_chasse_golem_crystal", "Briseur de Golems",
                "Detruisez des golems de cristal.",
                QuestType.CHASSE, QuestCategory.JOURNALIERE, QuestDifficulty.S,
                "golem crystal", 3,
                3000, 2000, null,
                null, null, 1440);
    }
}
