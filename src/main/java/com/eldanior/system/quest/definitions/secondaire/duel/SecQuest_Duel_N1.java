package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N1 : L'Epreuve du Debutant
 * Difficulte F - Gagner 1 duel
 */
public class SecQuest_Duel_N1 extends NpcDialogueQuest {

    public SecQuest_Duel_N1() {
        super(
                "sec_duel_1",
                "L'Epreuve du Debutant",
                "Un vieux maitre d'armes vous propose de tester vos reflexes en duel.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.F,
                null, 1,
                200, 500, null,
                null,
                "Quest_Npc_Secondaire_Duel_N1",

                List.of(
                    new DialoguePage("Gareth le Rouille",
                            "He, toi la-bas ! Tu as la demarche d'un aventurier, " +
                            "mais as-tu seulement deja croise le fer avec un autre combattant ?\n\n" +
                            "Je suis Gareth, ancien soldat de la garde royale. " +
                            "Mes os grincent mais mon oeil est encore vif.",
                            "Un vieil homme appuye sur une epee emousee"),

                    new DialoguePage("Gareth le Rouille",
                            "Le vrai combat ne s'apprend pas sur des mannequins de paille. " +
                            "Il faut sentir la pression d'un adversaire qui veut ta peau.\n\n" +
                            "Gagne un seul duel contre un autre aventurier, " +
                            "et je reconnaitrai que tu as au moins le courage de te battre.",
                            "Il pointe du doigt l'arene de duel"),

                    new DialoguePage("Gareth le Rouille",
                            "Allez, file ! Et reviens me voir quand tu auras prouve " +
                            "que tu sais tenir une arme face a un vrai adversaire.\n\n" +
                            "Un seul duel gagne, c'est tout ce que je demande.",
                            "Il crache par terre et s'assoit sur une caisse")
                ),
                null, null, 1440
        );
    }
}
