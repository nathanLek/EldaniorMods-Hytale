package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N15 - La Fortune d'Eldanior
 * Un sage ancien demande de constituer la plus grande fortune jamais vue.
 * Difficulte S - 500000 Or a collecter.
 */
public class SecQuest_Collection_N15 extends NpcDialogueQuest {

    public SecQuest_Collection_N15() {
        super(
                "sec_collection_15",
                "La Fortune d'Eldanior",
                "Le sage Tharion met au defi les plus grands aventuriers du monde.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                null, 500000,
                50000, 250000, null,
                null,
                "Quest_Npc_Secondaire_Collection_N15",

                List.of(
                    new DialoguePage("Sage Tharion",
                            "Vous etes venu jusqu'ici. Peu y parviennent.\n\n" +
                            "Je suis Tharion, gardien du Sanctuaire de la Prosperite. " +
                            "Depuis des siecles, ce lieu attend celui ou celle " +
                            "qui saura incarner la richesse supreme.",
                            "Un vieillard lumineux dans un temple dore"),

                    new DialoguePage("Sage Tharion",
                            "La richesse n'est pas une fin en soi, aventurier. " +
                            "C'est une mesure de votre determination, " +
                            "de votre endurance, de votre capacite a transformer " +
                            "le monde autour de vous.\n\n" +
                            "Chaque piece d'or que vous avez gagnee raconte une histoire. " +
                            "Un monstre vaincu, un donjon explore, un defi surmonte.",
                            "Des fresques dorees montrent l'histoire des grands heros"),

                    new DialoguePage("Sage Tharion",
                            "Le defi ultime : reunir 500000 pieces d'or.\n\n" +
                            "Personne n'a jamais accompli cet exploit. " +
                            "Ceux qui ont essaye ont abandonne, ou se sont perdus " +
                            "dans l'avarice.\n\n" +
                            "Mais si vous reussissez en gardant votre honneur intact, " +
                            "le Sanctuaire vous reconnaitra comme le Champion " +
                            "de la Prosperite. La plus grande recompense qui soit.",
                            "Le temple entier semble vibrer d'une energie doree")
                ),

                null,
                null,
                1440
        );
    }
}
