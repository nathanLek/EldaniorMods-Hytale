package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N4 - L'Investissement du Banquier
 * Un banquier propose un test de gestion financiere.
 * Difficulte E - 3500 Or a collecter.
 */
public class SecQuest_Collection_N4 extends NpcDialogueQuest {

    public SecQuest_Collection_N4() {
        super(
                "sec_collection_4",
                "L'Investissement du Banquier",
                "Le banquier Theomund veut tester votre sens des affaires.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                null, 3500,
                900, 1500, null,
                null,
                "Quest_Npc_Secondaire_Collection_N4",

                List.of(
                    new DialoguePage("Theomund",
                            "Bonjour, bonjour ! Entrez, prenez place.\n\n" +
                            "Vous avez l'air de quelqu'un qui comprend la valeur de l'or. " +
                            "Je me trompe rarement sur ces choses-la. Vingt ans dans la finance, " +
                            "ca affute le regard.",
                            "Un banquier elegant dans un bureau richement decore"),

                    new DialoguePage("Theomund",
                            "Voyez-vous, je cherche des partenaires fiables. " +
                            "Des gens capables de generer du capital rapidement.\n\n" +
                            "Pas des nobles qui heritent de papa, non. Des aventuriers " +
                            "qui savent se retrousser les manches.",
                            "Il tapote un registre epais rempli de chiffres"),

                    new DialoguePage("Theomund",
                            "Voici mon test : accumulez 3500 pieces d'or. " +
                            "Peu m'importe la methode, tant qu'elle est... legale. Enfin, " +
                            "relativement legale.\n\n" +
                            "Prouvez-moi que vous savez faire fructifier vos activites, " +
                            "et je vous ouvrirai des portes que vous ne soupconnez meme pas.",
                            "Il vous tend une carte de visite doree")
                ),

                null,
                null,
                1440
        );
    }
}
