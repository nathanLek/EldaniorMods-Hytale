package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N18 : Le Feu de la Forge
 * Difficulte S - Gagner 20 duels
 */
public class SecQuest_Duel_N18 extends NpcDialogueQuest {

    public SecQuest_Duel_N18() {
        super(
                "sec_duel_18",
                "Le Feu de la Forge",
                "Un forgeron legendaire n'offre ses armes qu'a ceux qui ont prouve leur maitrise au combat.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                null, 20,
                6000, 35000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N18",

                List.of(
                    new DialoguePage("Thalion le Forgeur d'Ames",
                            "On dit que mes lames chantent quand elles frappent. " +
                            "Que mon acier est beni par les dieux eux-memes.\n\n" +
                            "La verite est plus simple : je forge pour ceux qui meritent. " +
                            "Et meriter, ca se prouve par le combat, pas par les mots.",
                            "Un forgeron immense devant un four qui brule d'un feu bleu"),

                    new DialoguePage("Thalion le Forgeur d'Ames",
                            "Chaque lame que je cree porte un fragment de l'ame de son porteur. " +
                            "Si l'ame est faible, la lame sera terne.\n\n" +
                            "Vingt duels. C'est le nombre qu'il faut pour tremper une ame " +
                            "dans le feu du vrai combat. Chaque victoire te forge " +
                            "autant que je forge l'acier.",
                            "Il plonge une lame rougeoyante dans l'eau glacee"),

                    new DialoguePage("Thalion le Forgeur d'Ames",
                            "Reviens quand ton ame sera prete. Je le sentirai. " +
                            "Le feu de la forge ne ment jamais.\n\n" +
                            "Vingt victoires, aventurier. Pas une de moins.",
                            "Les flammes du four semblent danser en rythme avec ses paroles")
                ),
                null, null, 1440
        );
    }
}
