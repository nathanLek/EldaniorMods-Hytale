package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N16 extends NpcDialogueQuest {
    public SecQuest_Chasse_N16() {
        super(
                "sec_chasse_n16",
                "Chasse a l'Outlander",
                "Traquer les brutes Outlander qui rodent dans les ruines.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                "outlander brute", 10,
                2800, 2200, null,
                null, "Quest_Npc_Secondaire_Chasse_N16",
                List.of(
                        new DialoguePage("Dante le Sombre", "On m'appelle Dante. Le Sombre. Pas parce que je suis mechant, mais parce que je travaille dans l'ombre. Chasseur de primes, si tu preferes."),
                        new DialoguePage("Dante le Sombre", "Dix brutes Outlander patrouillent dans les ruines anciennes. Ces colosses sont dangereux et ils protegent quelque chose. Je ne sais pas quoi, mais ca vaut cher."),
                        new DialoguePage("Dante le Sombre", "Je t'offre tout ce que j'ai economise si tu les elimines. C'est au-dessus de mes capacites, mais pas des tiennes, j'en suis sur.")
                ),
                null, null, 1440
        );
    }
}
