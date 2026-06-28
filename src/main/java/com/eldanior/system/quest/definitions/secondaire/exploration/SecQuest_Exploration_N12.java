package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N12 : Le Tresor du Pirate Fantome.
 * Le fantome d'un pirate hante un rivage et guide les aventuriers vers ses coffres enfouis.
 * Difficulte A - 20 coffres a decouvrir.
 */
public class SecQuest_Exploration_N12 extends NpcDialogueQuest {

    public SecQuest_Exploration_N12() {
        super(
                "sec_exploration_12",
                "Le Tresor du Pirate Fantome",
                "Le fantome d'un ancien pirate vous supplie de retrouver ses coffres enfouis.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                null, 20,
                4500, 18000, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N12",

                List.of(
                    new DialoguePage("Capitaine Morven",
                            "Oooh... Un vivant ! Enfin quelqu'un qui peut m'entendre !\n\n" +
                            "Je suis le Capitaine Morven, terreur des mers d'Eldanior... " +
                            "enfin, je l'etais. Maintenant je suis un fantome condamne " +
                            "a errer tant que mon tresor ne sera pas retrouve.",
                            "Une silhouette spectrale en tenue de pirate"),

                    new DialoguePage("Capitaine Morven",
                            "Avant de mourir, j'ai disperse mon butin dans des coffres " +
                            "un peu partout. Je me disais que personne ne les trouverait " +
                            "jamais tous.\n\n" +
                            "Resultat : personne ne les a trouves, et moi je suis " +
                            "coince ici pour l'eternite. Belle ironie, pas vrai ?",
                            "Il rit d'un rire spectral"),

                    new DialoguePage("Capitaine Morven",
                            "Trouve 20 coffres, n'importe lesquels. Ca devrait suffire " +
                            "a briser cette malediction... j'espere.\n\n" +
                            "Et garde tout ce que tu trouves. Un pirate mort " +
                            "n'a pas besoin d'or. Juste de liberte.",
                            "Il pointe vers l'horizon d'une main translucide")
                ),

                null,
                null,
                1440
        );
    }
}
