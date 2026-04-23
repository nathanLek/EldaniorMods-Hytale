package com.eldanior.system.quest.definitions.secondaire;

import com.eldanior.system.quest.*;

public class SideQuest_Duelliste extends QuestModel {
    public SideQuest_Duelliste() {
        super("side_duel_1", "Duelliste",
                "Prouvez votre valeur en duel.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                null, 3,
                1500, 3000, null,
                null, null);
    }
}