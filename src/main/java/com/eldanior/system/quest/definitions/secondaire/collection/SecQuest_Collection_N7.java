package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N7 - Les Taxes du Seigneur
 * Un percepteur collecte les taxes pour le seigneur local.
 * Difficulte C - 10000 Or a collecter.
 */
public class SecQuest_Collection_N7 extends NpcDialogueQuest {

    public SecQuest_Collection_N7() {
        super(
                "sec_collection_7",
                "Les Taxes du Seigneur",
                "Le percepteur Gaston exige le paiement des taxes arrieres.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                null, 10000,
                2500, 5000, null,
                null,
                "Quest_Npc_Secondaire_Collection_N7",

                List.of(
                    new DialoguePage("Gaston",
                            "Halte ! Vous la, l'aventurier !\n\n" +
                            "Au nom de Sire Beaumont, seigneur de cette province, " +
                            "je suis charge de collecter les taxes de passage. " +
                            "Tout individu exerçant une activite lucrative sur ces terres " +
                            "doit s'acquitter de ses obligations.",
                            "Un homme austere en uniforme officiel"),

                    new DialoguePage("Gaston",
                            "Ne faites pas cette tete. Les taxes financent la securite des routes, " +
                            "l'entretien des ponts, et la milice qui vous protege " +
                            "pendant que vous dormez dans les auberges.\n\n" +
                            "Sans taxes, pas d'ordre. Sans ordre, c'est le chaos.",
                            "Il consulte un epais registre avec agacement"),

                    new DialoguePage("Gaston",
                            "Selon mes calculs, vous devez 10000 pieces d'or " +
                            "en taxes arrieres.\n\n" +
                            "Oui, c'est beaucoup. Mais les reglements sont clairs. " +
                            "Payez, et vous serez en regle. Le seigneur recompense " +
                            "les bons contribuables, vous savez.\n\n" +
                            "Revenez quand vous aurez la somme. Et pas d'entourloupes !",
                            "Il tamponne un formulaire avec autorite")
                ),

                null,
                null,
                1440
        );
    }
}
