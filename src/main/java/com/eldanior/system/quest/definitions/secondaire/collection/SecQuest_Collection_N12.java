package com.eldanior.system.quest.definitions.secondaire.collection;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Collection N12 - Le Tribut de Guerre
 * Un general demande des fonds pour financer l'armee.
 * Difficulte A - 80000 Or a collecter.
 */
public class SecQuest_Collection_N12 extends NpcDialogueQuest {

    public SecQuest_Collection_N12() {
        super(
                "sec_collection_12",
                "Le Tribut de Guerre",
                "Le General Kael a besoin de fonds pour equiper l'armee du front.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                null, 80000,
                14000, 40000, null,
                null,
                "Quest_Npc_Secondaire_Collection_N12",

                List.of(
                    new DialoguePage("General Kael",
                            "Repos, soldat. Ou devrais-je dire... aventurier.\n\n" +
                            "Je suis le General Kael, commandant de la Troisieme Legion. " +
                            "Nos troupes tiennent le front nord depuis six mois, " +
                            "mais la situation devient critique.",
                            "Un homme en armure usee par les batailles"),

                    new DialoguePage("General Kael",
                            "Les renforts n'arrivent pas. L'equipement est a bout. " +
                            "Mes hommes se battent avec des epees ebrechees " +
                            "et des boucliers fendus.\n\n" +
                            "Le roi promet des fonds, mais les promesses " +
                            "n'arretent pas les fleches ennemies.",
                            "Il montre une carte de bataille couverte de marques rouges"),

                    new DialoguePage("General Kael",
                            "J'ai besoin de 80000 pieces d'or pour reequiper mes troupes. " +
                            "Armures, armes, vivres, potions de soin.\n\n" +
                            "Si le front tombe, les terres d'Eldanior seront envahies. " +
                            "Chaque piece d'or que vous apporterez sauvera des vies.\n\n" +
                            "Etes-vous avec nous, aventurier ?",
                            "Il vous serre la main avec la poigne d'un guerrier")
                ),

                null,
                null,
                1440
        );
    }
}
