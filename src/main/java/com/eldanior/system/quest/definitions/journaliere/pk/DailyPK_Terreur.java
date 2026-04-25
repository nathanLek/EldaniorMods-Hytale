package com.eldanior.system.quest.definitions.journaliere.pk;

import com.eldanior.system.quest.*;

/**
 * Quete journaliere PK : Tuer beaucoup de monstres en tant que criminel.
 * Reservee aux joueurs PK.
 */
public class DailyPK_Terreur extends QuestModel {
    public DailyPK_Terreur() {
        super("daily_pk_terreur", "Terreur Nocturne",
                "Repandez la destruction sur votre passage.",
                QuestType.MASSACRE, QuestCategory.JOURNALIERE, QuestDifficulty.C,
                null, 50,
                1250, 750, null,
                null, null, 1440);
    }
}