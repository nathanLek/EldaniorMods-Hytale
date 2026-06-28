package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N14 extends NpcDialogueQuest {
    public SecQuest_Chasse_N14() {
        super(
                "sec_chasse_n14",
                "Necromanciens du Vide",
                "Eliminer les necromanciens qui corrompent la terre.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                "necromancer", 8,
                4000, 3000, null,
                null, "Quest_Npc_Secondaire_Chasse_N14",
                List.of(
                        new DialoguePage("Aldric le Paladin", "Paladin Aldric de l'Ordre Sacre. Je suis en mission divine mais j'ai besoin d'aide. Huit necromanciens se sont installes dans les catacombes."),
                        new DialoguePage("Aldric le Paladin", "Huit necromanciens relevent les morts a un rythme alarmant. Chaque jour qui passe, leur armee de squelettes grossit. Bientot, on ne pourra plus les arreter."),
                        new DialoguePage("Aldric le Paladin", "La lumiere vaincra les tenebres, mais il me faut un compagnon d'armes. Ensemble, nous purifierons ces lieux maudits.")
                ),
                null, null, 1440
        );
    }
}
