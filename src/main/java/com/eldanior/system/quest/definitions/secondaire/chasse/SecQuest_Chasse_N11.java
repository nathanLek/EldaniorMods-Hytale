package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N11 extends NpcDialogueQuest {
    public SecQuest_Chasse_N11() {
        super(
                "sec_chasse_n11",
                "Meute Enragee",
                "Eliminer les ferans enrages qui terrorisent les plaines.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                "feran", 20,
                1000, 700, null,
                null, "Quest_Npc_Secondaire_Chasse_N11",
                List.of(
                        new DialoguePage("Mira la Rodeuse", "Mira, rodeuse des plaines. Ca fait des annees que j'observe les ferans. Ce sont normalement des creatures pacifiques."),
                        new DialoguePage("Mira la Rodeuse", "Normalement pacifiques, oui. Mais quelque chose les a rendus fous. Ils attaquent tout ce qui bouge, meme entre eux. C'est contre nature."),
                        new DialoguePage("Mira la Rodeuse", "Vingt devraient suffire pour proteger les voyageurs. Ca me brise le coeur, mais on n'a pas le choix.")
                ),
                null, null, 1440
        );
    }
}
