package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N13 - Le Coffre du Roi Dechu
 * Un tresorier royal cherche a reconstituer le tresor perdu du royaume.
 * Difficulte S - 120000 Or a collecter.
 */
public class SecQuest_Collection_N13 extends NpcDialogueQuest {

    public SecQuest_Collection_N13() {
        super(
                "sec_collection_13",
                "Le Coffre du Roi Dechu",
                "Le tresorier Aldwin veut reconstituer le tresor royal disparu.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                null, 120000,
                20000, 60000, null,
                null,
                "Quest_Npc_Secondaire_Collection_N13",

                List.of(
                    new DialoguePage("Aldwin",
                            "Vous ne me connaissez pas, mais moi je vous connais. " +
                            "Votre reputation vous precede, aventurier.\n\n" +
                            "Je suis Aldwin, dernier tresorier du Roi Dechu. " +
                            "Quand le royaume est tombe, j'ai reussi a sauver les registres. " +
                            "Mais pas le tresor.",
                            "Un vieil homme voute, entoure de livres de comptes anciens"),

                    new DialoguePage("Aldwin",
                            "Le tresor royal a ete pille, disperse, fondu. " +
                            "Des decennies d'accumulation, parties en fumee.\n\n" +
                            "Mais j'ai un plan. Si quelqu'un pouvait reconstituer ne serait-ce " +
                            "qu'une partie de ce tresor, nous pourrions restaurer la monarchie. " +
                            "Un vrai roi, cette fois. Pas un tyran.",
                            "Il ouvre un coffre vide, symbole de ce qui a ete perdu"),

                    new DialoguePage("Aldwin",
                            "120000 pieces d'or. C'est la somme necessaire pour " +
                            "amorcer la restauration.\n\n" +
                            "Je sais que c'est colossal. Mais vous n'etes pas quelqu'un d'ordinaire, " +
                            "n'est-ce pas ? Les heros ordinaires ne viennent pas jusqu'ici.\n\n" +
                            "Reunissez cette fortune, et vous entrerez dans la legende " +
                            "comme celui qui a ressuscite un royaume.",
                            "Ses yeux brillent d'une determination inebranmable")
                ),

                null,
                null,
                1440
        );
    }
}
