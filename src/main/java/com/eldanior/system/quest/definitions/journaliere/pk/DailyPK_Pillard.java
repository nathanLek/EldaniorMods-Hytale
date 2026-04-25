package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

/**
 * Quete journaliere PK : Accumuler de l'or par le pillage.
 * Reservee aux joueurs PK.
 */
public class DailyPK_Pillard extends QuestModel {
    public DailyPK_Pillard() {
        super("daily_pk_pillard", "Pillard Infame",
                "Accumulez de l'or par tous les moyens.",
                QuestType.COLLECTION, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 8000,
                1250, 750, null,
                null, null, 1440);
    }
}