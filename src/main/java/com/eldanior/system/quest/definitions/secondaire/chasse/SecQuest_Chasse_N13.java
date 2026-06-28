package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N13 extends NpcDialogueQuest {
    public SecQuest_Chasse_N13() {
        super(
                "sec_chasse_n13",
                "Pigeons Voraces",
                "Chasser les pigeons qui devvorent les recoltes.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.F,
                "pigeon", 15,
                200, 100, null,
                null, "Quest_Npc_Secondaire_Chasse_N13",
                List.of(
                        new DialoguePage("Nessa l'Archere", "Salut ! Nessa, archere du village. Bon, j'ai un probleme un peu... embarrassant a te soumettre."),
                        new DialoguePage("Nessa l'Archere", "Ces volatiles devorent nos semences a peine plantees. Les fermiers sont furieux et mes fleches passent a cote a chaque fois. Ces maudits oiseaux sont rapides."),
                        new DialoguePage("Nessa l'Archere", "Quinze pigeons en moins et les fermiers pourront enfin semer tranquilles. La recompense est modeste mais le travail est simple.")
                ),
                null, null, 1440
        );
    }
}
