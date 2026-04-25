package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

/**
 * Quete journaliere PK : Gagner des duels en tant que criminel.
 * Reservee aux joueurs PK.
 */
public class DailyPK_DuelSombre extends QuestModel {
    public DailyPK_DuelSombre() {
        super("daily_pk_duel", "Duel Sombre",
                "Prouvez votre superiorite en duel.",
                QuestType.DUEL, QuestCategory.JOURNALIERE, QuestDifficulty.A,
                null, 2,
                2285, 1715, null,
                null, null, 1440);
    }
}