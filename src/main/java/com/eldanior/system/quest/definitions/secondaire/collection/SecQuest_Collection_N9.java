package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N9 - La Construction de la Tour
 * Un architecte nain a besoin de fonds pour construire une tour de guet.
 * Difficulte B - 25000 Or a collecter.
 */
public class SecQuest_Collection_N9 extends NpcDialogueQuest {

    public SecQuest_Collection_N9() {
        super(
                "sec_collection_9",
                "La Construction de la Tour",
                "L'architecte Bromdar recrute des mecenes pour batir une tour de defense.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                null, 25000,
                5000, 12000, null,
                null,
                "Quest_Npc_Secondaire_Collection_N9",

                List.of(
                    new DialoguePage("Bromdar",
                            "Par la barbe de mes ancetres ! Encore un aventurier sans le sou ?\n\n" +
                            "Je suis Bromdar, maitre architecte. Troisieme generation. " +
                            "Mon grand-pere a bati la forteresse de Kharaz-Dum, " +
                            "et moi, je vais construire quelque chose d'encore plus grand.",
                            "Un nain trapu devant des plans architecturaux gigantesques"),

                    new DialoguePage("Bromdar",
                            "Une tour de guet ! Pas n'importe laquelle. " +
                            "Une tour qui percera les nuages, visible depuis trois provinces.\n\n" +
                            "Le probleme ? L'or. Toujours l'or. Les materiaux coutent cher, " +
                            "la main-d'oeuvre aussi. Et les politiciens refusent de financer " +
                            "quoi que ce soit de... grandiose.",
                            "Il deploie un plan detaille impressionnant"),

                    new DialoguePage("Bromdar",
                            "Il me faut 25000 pieces d'or pour lancer le chantier.\n\n" +
                            "Vous investissez, et en echange, je vous garantis la meilleure " +
                            "vue d'Eldanior. Et une belle recompense, evidemment.\n\n" +
                            "Alors ? Vous etes un batisseur ou un simple passant ?",
                            "Il vous tend un marteau ceremoniel")
                ),

                null,
                null,
                1440
        );
    }
}
