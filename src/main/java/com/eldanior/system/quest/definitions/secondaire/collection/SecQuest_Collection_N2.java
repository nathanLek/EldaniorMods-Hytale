package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N2 - La Dime du Temple
 * Une pretresse demande une offrande pour restaurer le temple.
 * Difficulte F - 1000 Or a collecter.
 */
public class SecQuest_Collection_N2 extends NpcDialogueQuest {

    public SecQuest_Collection_N2() {
        super(
                "sec_collection_2",
                "La Dime du Temple",
                "La pretresse Elyna a besoin de fonds pour restaurer le temple sacre.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.F,
                null, 1000,
                400, 500, null,
                null,
                "Quest_Npc_Secondaire_Collection_N2",

                List.of(
                    new DialoguePage("Elyna",
                            "Que la lumiere vous guide, voyageur. Vous voyez l'etat de ce temple ?\n\n" +
                            "Les murs s'effondrent, les vitraux sont brises, et les fideles " +
                            "n'osent plus venir prier. Nous avons besoin d'aide.",
                            "Une pretresse devant un temple en ruine"),

                    new DialoguePage("Elyna",
                            "Le conseil du village a refuse de financer les reparations. " +
                            "Ils disent que la guerre coute deja trop cher.\n\n" +
                            "Mais sans ce temple, qui protegera les ames des guerriers tombes au combat ? " +
                            "Qui benira les recoltes ?",
                            "Elle montre les degats avec tristesse"),

                    new DialoguePage("Elyna",
                            "Si vous pouviez reunir 1000 pieces d'or pour contribuer aux reparations, " +
                            "ce serait un geste immense.\n\n" +
                            "Les dieux n'oublient jamais ceux qui les servent. " +
                            "Votre generosite sera recompensee, je vous le promets.",
                            "Elle joint les mains en priere")
                ),

                null,
                null,
                1440
        );
    }
}
