package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N19 : Le Dernier Defi
 * Difficulte S - Gagner 25 duels
 */
public class SecQuest_Duel_N19 extends NpcDialogueQuest {

    public SecQuest_Duel_N19() {
        super(
                "sec_duel_19",
                "Le Dernier Defi",
                "Un combattant legendaire sur son lit de mort lance un ultime defi.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                null, 25,
                7000, 40000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N19",

                List.of(
                    new DialoguePage("Silas le Fantome",
                            "On m'appelait le Fantome parce que personne ne me voyait venir. " +
                            "Deux cents duels, pas une seule defaite. Un record " +
                            "que personne n'a jamais approche.\n\n" +
                            "Mais le temps fait ce que les lames n'ont pas pu faire. " +
                            "Je suis mourant.",
                            "Un vieil homme emacié, allonge sur un lit, mais au regard vif"),

                    new DialoguePage("Silas le Fantome",
                            "Avant de partir, je veux savoir qu'il existe quelqu'un " +
                            "capable de reprendre le flambeau. Pas un imitateur. Un vrai.\n\n" +
                            "Vingt-cinq duels. C'est le quart de mon record. " +
                            "Si tu arrives a ca, tu as le potentiel pour me depasser un jour.",
                            "Il tousse mais son regard reste fixe sur vous"),

                    new DialoguePage("Silas le Fantome",
                            "Je n'ai rien a t'offrir sauf mon respect et ma connaissance. " +
                            "Quand tu auras vingt-cinq victoires, reviens.\n\n" +
                            "Je t'apprendrai la derniere technique du Fantome. " +
                            "Celle que je n'ai jamais enseignee a personne.",
                            "Il ferme les yeux et semble economiser chaque souffle")
                ),
                null, null, 1440
        );
    }
}
