package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

/**
 * Quete journaliere PK : Assassiner des joueurs.
 * Reservee aux joueurs PK.
 */
public class DailyPK_Assassin extends QuestModel {
    public DailyPK_Assassin() {
        super("daily_pk_assassin", "Assassin du Jour",
                "Eliminez des joueurs pour prouver votre valeur.",
                QuestType.EXECUTION, QuestCategory.JOURNALIERE, QuestDifficulty.B,
                null, 3,
                2000, 1200, null,
                null, null, 1440);
    }

    @Override
    public String getObjectiveText() {
        return "Eliminer 3 joueurs";
    }
}