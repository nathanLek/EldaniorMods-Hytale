package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N11 - L'Enchere du Dragon
 * Un collectionneur excentrique organise une vente aux encheres speciale.
 * Difficulte A - 60000 Or a collecter.
 */
public class SecQuest_Collection_N11 extends NpcDialogueQuest {

    public SecQuest_Collection_N11() {
        super(
                "sec_collection_11",
                "L'Enchere du Dragon",
                "Le collectionneur Maximilien met aux encheres un artefact draconique.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                null, 60000,
                10000, 30000, null,
                null,
                "Quest_Npc_Secondaire_Collection_N11",

                List.of(
                    new DialoguePage("Maximilien",
                            "Ah, un nouveau visage ! Bienvenue dans ma galerie privee.\n\n" +
                            "Je suis Maximilien de Haute-Pierre, collectionneur " +
                            "d'antiquites et d'artefacts magiques. " +
                            "Certains me trouvent excentrique. Je prefere le terme 'visionnaire'.",
                            "Un homme flamboyant dans un manoir rempli de curiosites"),

                    new DialoguePage("Maximilien",
                            "J'organise bientot une vente aux encheres tres speciale. " +
                            "La piece maitresse ? Un oeuf de dragon petrifie, " +
                            "datant de l'ere primordiale.\n\n" +
                            "Bien sur, seuls ceux qui ont les moyens financiers " +
                            "necessaires peuvent participer. Question de standing.",
                            "Il devoile un coffret orne contenant un oeuf brillant"),

                    new DialoguePage("Maximilien",
                            "Le prix de reserve est de 60000 pieces d'or. " +
                            "Pas un sou de moins.\n\n" +
                            "Reunissez cette somme et revenez me voir. " +
                            "Je vous reserverai un siege au premier rang. " +
                            "Et entre nous... l'oeuf vaut bien plus que cela.\n\n" +
                            "Les dragons ne sont jamais vraiment eteints, n'est-ce pas ?",
                            "Un sourire enigmatique eclaire son visage")
                ),

                null,
                null,
                1440
        );
    }
}
