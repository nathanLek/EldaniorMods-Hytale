package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N5 - La Rancon du Capitaine
 * Un capitaine pirate demande une rancon pour liberer un prisonnier.
 * Difficulte D - 5000 Or a collecter.
 */
public class SecQuest_Collection_N5 extends NpcDialogueQuest {

    public SecQuest_Collection_N5() {
        super(
                "sec_collection_5",
                "La Rancon du Capitaine",
                "Le capitaine Varn retient un innocent en otage contre rancon.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                null, 5000,
                1200, 2500, null,
                null,
                "Quest_Npc_Secondaire_Collection_N5",

                List.of(
                    new DialoguePage("Capitaine Varn",
                            "Arretez-vous la, l'ami. Vous n'avez pas l'air d'un garde, " +
                            "et c'est tant mieux pour vous.\n\n" +
                            "Je suis le Capitaine Varn. Peut-etre avez-vous entendu parler de moi ? " +
                            "Non ? Tant pis, vous allez apprendre.",
                            "Un homme balafre dans un manteau de cuir use"),

                    new DialoguePage("Capitaine Varn",
                            "J'ai un... invite, dans ma cale. Un marchand qui voyageait " +
                            "avec plus de courage que de bon sens.\n\n" +
                            "Sa famille veut le recuperer, mais elle n'a pas les moyens. " +
                            "Alors peut-etre que vous, heroique aventurier, " +
                            "pourriez reunir la somme ?",
                            "Il joue avec un poignard d'un air desinvolte"),

                    new DialoguePage("Capitaine Varn",
                            "5000 pieces d'or. C'est le prix de la liberte de ce pauvre homme.\n\n" +
                            "Ne me regardez pas comme ca. Les affaires sont les affaires. " +
                            "Et au moins, moi, je negocie. D'autres l'auraient jete par-dessus bord.\n\n" +
                            "Revenez avec l'or, et tout le monde sera content.",
                            "Il sourit en montrant une dent en or")
                ),

                null,
                null,
                1440
        );
    }
}
