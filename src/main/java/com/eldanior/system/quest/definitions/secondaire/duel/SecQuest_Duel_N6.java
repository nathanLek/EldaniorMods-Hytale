package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N6 : La Voie du Sabre
 * Difficulte D - Gagner 5 duels
 */
public class SecQuest_Duel_N6 extends NpcDialogueQuest {

    public SecQuest_Duel_N6() {
        super(
                "sec_duel_6",
                "La Voie du Sabre",
                "Un maitre d'armes etranger enseigne une technique ancienne a ceux qui la meritent.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                null, 5,
                1200, 3000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N6",

                List.of(
                    new DialoguePage("Maitre Kenzo",
                            "Je viens d'un pays au-dela des mers, ou le combat est un art sacre. " +
                            "Ici, je vois des brutes qui frappent sans reflexion.\n\n" +
                            "Le vrai combat est une danse. Chaque mouvement a un sens, " +
                            "chaque parade ouvre une opportunite.",
                            "Un homme en tenue sombre, un sabre courbe a la ceinture"),

                    new DialoguePage("Maitre Kenzo",
                            "Si tu veux apprendre la Voie du Sabre, il faut d'abord prouver " +
                            "que tu sais combattre avec ton propre style.\n\n" +
                            "Cinq duels. Observe tes adversaires, apprends de chaque echange. " +
                            "La victoire n'est que la consequence de la comprehension.",
                            "Il depose son sabre sur un support en bois"),

                    new DialoguePage("Maitre Kenzo",
                            "Ne cherche pas la force brute. Cherche le moment juste. " +
                            "Quand tu auras gagne cinq fois, reviens.\n\n" +
                            "Alors seulement, je t'enseignerai.",
                            "Il ferme les yeux et se met en position de meditation")
                ),
                null, null, 1440
        );
    }
}
