package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N20 : L'Apotheose du Guerrier
 * Difficulte S - Gagner 30 duels - Donne le titre "Lame Legendaire"
 */
public class SecQuest_Duel_N20 extends NpcDialogueQuest {

    public SecQuest_Duel_N20() {
        super(
                "sec_duel_20",
                "L'Apotheose du Guerrier",
                "L'esprit d'un ancien roi guerrier apparait pour couronner le plus grand combattant d'Eldanior.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                null, 30,
                10000, 50000, "lame_legendaire",
                null,
                "Quest_Npc_Secondaire_Duel_N20",

                List.of(
                    new DialoguePage("Spectre du Roi Vaelorn",
                            "Mortel... Tu foules un sol sacre. Ici repose la memoire " +
                            "de tous les grands guerriers d'Eldanior.\n\n" +
                            "Je suis Vaelorn, roi guerrier de la Premiere Ere. " +
                            "Mon esprit veille sur l'arene depuis des millenaires, " +
                            "attendant celui ou celle qui sera digne du titre supreme.",
                            "Un spectre lumineux en armure royale, couronne sur la tete"),

                    new DialoguePage("Spectre du Roi Vaelorn",
                            "Le titre de Lame Legendaire n'a ete accorde qu'une seule fois " +
                            "dans toute l'histoire. A moi. De mon vivant.\n\n" +
                            "Trente duels victorieux. C'est l'epreuve ultime. " +
                            "Chaque victoire sera gravee dans la pierre de ce sanctuaire. " +
                            "Chaque defaite sera oubliee, car seule la perseverance compte.",
                            "Des runes anciennes s'illuminent sur les murs"),

                    new DialoguePage("Spectre du Roi Vaelorn",
                            "Si tu triomphes, je te transmettrai mon titre a travers les ages. " +
                            "Tu seras la Lame Legendaire, reconnue par les vivants et les morts.\n\n" +
                            "Trente duels, guerrier. Montre-moi que les mortels " +
                            "d'aujourd'hui valent ceux d'autrefois.\n\n" +
                            "Que l'acier tranche et que l'ame resiste !",
                            "Le spectre leve son epee spectrale vers le ciel")
                ),
                null, null, 1440
        );
    }
}
