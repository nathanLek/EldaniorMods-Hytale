package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N8 - Le Marche Noir
 * Un informateur louche propose un deal juteux... si vous avez les fonds.
 * Difficulte C - 15000 Or a collecter.
 */
public class SecQuest_Collection_N8 extends NpcDialogueQuest {

    public SecQuest_Collection_N8() {
        super(
                "sec_collection_8",
                "Le Marche Noir",
                "L'informateur Silas a des renseignements precieux, mais ils ont un prix.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                null, 15000,
                3500, 7500, null,
                null,
                "Quest_Npc_Secondaire_Collection_N8",

                List.of(
                    new DialoguePage("Silas",
                            "Psst... par ici, l'ami. Dans l'ombre, oui.\n\n" +
                            "On m'appelle Silas. Je vends des informations. " +
                            "Le genre d'informations qui peuvent changer une vie... " +
                            "ou y mettre fin, selon le point de vue.",
                            "Un homme encapuchonne dans une ruelle sombre"),

                    new DialoguePage("Silas",
                            "J'ai un contact au marche noir qui peut vous fournir " +
                            "des choses introuvables ailleurs. Armes rares, armures enchantees, " +
                            "potions interdites...\n\n" +
                            "Mais ce genre de commerce demande du capital. " +
                            "Beaucoup de capital.",
                            "Il jette des coups d'oeil nerveux autour de lui"),

                    new DialoguePage("Silas",
                            "15000 pieces d'or. C'est le ticket d'entree " +
                            "pour acceder au marche noir.\n\n" +
                            "Une fois que vous aurez cette somme, revenez me voir. " +
                            "Je vous presenterai aux bonnes personnes. " +
                            "Et croyez-moi, l'investissement en vaut la peine.\n\n" +
                            "Mais ne parlez de ca a personne. Compris ?",
                            "Il disparait presque dans les tenebres")
                ),

                null,
                null,
                1440
        );
    }
}
