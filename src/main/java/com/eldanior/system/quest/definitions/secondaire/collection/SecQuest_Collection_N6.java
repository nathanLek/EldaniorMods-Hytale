package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N6 - Le Tresor de Famille
 * Une veuve demande de l'aide pour racheter l'heritage familial.
 * Difficulte D - 7500 Or a collecter.
 */
public class SecQuest_Collection_N6 extends NpcDialogueQuest {

    public SecQuest_Collection_N6() {
        super(
                "sec_collection_6",
                "Le Tresor de Famille",
                "Dame Isolde veut racheter la relique familiale vendue par son defunt mari.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                null, 7500,
                1800, 3500, null,
                null,
                "Quest_Npc_Secondaire_Collection_N6",

                List.of(
                    new DialoguePage("Dame Isolde",
                            "Pardonnez mon audace de vous aborder ainsi, aventurier.\n\n" +
                            "Je suis Dame Isolde, de la maison Varendel. Enfin... " +
                            "ce qu'il en reste. Mon epoux, que les dieux gardent son ame, " +
                            "a dilapide notre fortune avant de mourir.",
                            "Une femme noble aux vetements uses mais dignes"),

                    new DialoguePage("Dame Isolde",
                            "Il a vendu notre relique familiale -- le Pendentif de Varendel -- " +
                            "a un collectionneur de la capitale. Sans cet objet, " +
                            "ma famille perd son titre et ses terres.\n\n" +
                            "Le collectionneur accepte de le revendre, " +
                            "mais il demande une somme astronomique.",
                            "Elle essuie une larme discrete"),

                    new DialoguePage("Dame Isolde",
                            "Il me faut 7500 pieces d'or pour racheter le Pendentif.\n\n" +
                            "Je sais que c'est beaucoup demander a un inconnu, " +
                            "mais les Varendel n'oublient jamais une dette. " +
                            "Si vous m'aidez, ma famille vous sera eternellement reconnaissante.\n\n" +
                            "Acceptez-vous de m'aider ?",
                            "Elle vous regarde avec un melange d'espoir et de fierte")
                ),

                null,
                null,
                1440
        );
    }
}
