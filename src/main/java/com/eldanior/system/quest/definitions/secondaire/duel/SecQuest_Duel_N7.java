package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N7 : Freres d'Armes
 * Difficulte D - Gagner 10 duels
 */
public class SecQuest_Duel_N7 extends NpcDialogueQuest {

    public SecQuest_Duel_N7() {
        super(
                "sec_duel_7",
                "Freres d'Armes",
                "Un veteran cherche un compagnon d'entrainement digne de ce nom.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                null, 10,
                1500, 5000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N7",

                List.of(
                    new DialoguePage("Bjorn Poigne-de-Fer",
                            "Tu sais ce qui manque dans ce monde ? Des gens fiables. " +
                            "Des combattants sur qui tu peux compter quand la tempete eclate.\n\n" +
                            "J'ai perdu mon frere d'armes l'hiver dernier. " +
                            "Un troll des montagnes. Depuis, je cherche quelqu'un " +
                            "qui tient la route au combat.",
                            "Un colosse barbu avec un marteau de guerre sur l'epaule"),

                    new DialoguePage("Bjorn Poigne-de-Fer",
                            "Dix duels. Voila ce qu'il faut pour me convaincre. " +
                            "Pas parce que c'est un chiffre rond, mais parce qu'apres dix combats, " +
                            "on sait vraiment ce qu'un homme vaut.\n\n" +
                            "Tu apprendras a lire ton ennemi, a anticiper, a encaisser.",
                            "Il frappe son poing dans sa paume"),

                    new DialoguePage("Bjorn Poigne-de-Fer",
                            "Reviens avec dix victoires, et je t'offrirai une place " +
                            "a ma table et dans ma compagnie.\n\n" +
                            "Les Poignes-de-Fer ne recrutent pas des mauviettes !",
                            "Il eclate d'un rire tonitruant")
                ),
                null, null, 1440
        );
    }
}
