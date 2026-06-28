package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N9 : L'Ombre de l'Arene
 * Difficulte C - Gagner 10 duels
 */
public class SecQuest_Duel_N9 extends NpcDialogueQuest {

    public SecQuest_Duel_N9() {
        super(
                "sec_duel_9",
                "L'Ombre de l'Arene",
                "Un mysterieux individu masque propose un defi aux plus braves.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                null, 10,
                1500, 6000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N9",

                List.of(
                    new DialoguePage("L'Ombre",
                            "On m'appelle l'Ombre. Je n'ai pas de nom, pas de passe. " +
                            "Je n'existe que dans l'arene.\n\n" +
                            "J'observe les combattants depuis des mois. " +
                            "La plupart sont mediocres. Mais toi... tu as quelque chose.",
                            "Une silhouette encapuchonnee dans l'ombre d'un pilier"),

                    new DialoguePage("L'Ombre",
                            "Dix victoires en duel. C'est mon test. Ceux qui y parviennent " +
                            "sont dignes de connaitre les secrets de l'arene.\n\n" +
                            "Les angles morts, les failles dans chaque style de combat, " +
                            "les techniques que personne n'enseigne ouvertement...",
                            "Ses yeux brillent sous la capuche"),

                    new DialoguePage("L'Ombre",
                            "Dix victoires. Pas de limites de temps, pas de regles. " +
                            "Juste toi et ton instinct.\n\n" +
                            "Quand tu auras reussi, retrouve-moi ici. A l'ombre.",
                            "La silhouette se fond dans l'obscurite")
                ),
                null, null, 1440
        );
    }
}
