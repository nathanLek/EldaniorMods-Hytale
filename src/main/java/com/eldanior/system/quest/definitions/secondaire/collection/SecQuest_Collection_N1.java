package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N1 - Les Premieres Economies
 * Un vieux marchand demande au joueur de reunir un petit pecule.
 * Difficulte F - 500 Or a collecter.
 */
public class SecQuest_Collection_N1 extends NpcDialogueQuest {

    public SecQuest_Collection_N1() {
        super(
                "sec_collection_1",
                "Les Premieres Economies",
                "Le vieux marchand Aldric vous enseigne les bases de la fortune.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.F,
                null, 500,
                200, 250, null,
                null,
                "Quest_Npc_Secondaire_Collection_N1",

                List.of(
                    new DialoguePage("Aldric",
                            "Ah, un jeune aventurier ! Dis-moi, combien d'or as-tu dans tes poches ?\n\n" +
                            "Pas grand-chose, je parie. C'est le probleme avec vous, les heros en herbe. " +
                            "Vous courez apres les monstres, mais vous ne savez meme pas compter vos pieces.",
                            "Un vieux marchand derriere un comptoir poussieureux"),

                    new DialoguePage("Aldric",
                            "Ecoute, je vais te donner un conseil que mon pere m'a donne : " +
                            "l'or est le nerf de la guerre.\n\n" +
                            "Sans or, pas d'equipement. Sans equipement, pas de survie. " +
                            "C'est aussi simple que ca.",
                            "Il fait tinter quelques pieces dans sa main"),

                    new DialoguePage("Aldric",
                            "Voici ce que je te propose : reuni 500 pieces d'or. " +
                            "Peu importe comment tu les gagnes. Chasse des monstres, vends du loot, " +
                            "explore des donjons...\n\n" +
                            "Quand tu auras cette somme, reviens me voir. " +
                            "Je te recompenserai pour ta perseverance.",
                            "Il vous tend un petit carnet de comptes")
                ),

                null,
                null,
                1440
        );
    }
}
