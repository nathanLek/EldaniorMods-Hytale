package com.eldanior.system.quest.definitions.secondaire.minage;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire #22 : Veines de Cuivre
 * Miner 200 ore_copper pour Petra la Geologue.
 */
public class SecQuest_Minage_N2 extends NpcDialogueQuest {

    public SecQuest_Minage_N2() {
        super(
                "sec_minage_n2",
                "Veines de Cuivre",
                "Minez 200 minerais de cuivre pour Petra la Geologue.",
                QuestType.MINAGE, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                "ore_copper", 200,
                600, 400, null,
                null,
                "Quest_Npc_Secondaire_Minage_N2",

                List.of(
                    new DialoguePage("Petra la Geologue",
                            "Petra, geologue de l'Academie. J'etudie les veines de cuivre de la region. Mes echantillons ont ete detruits dans un eboulement."),
                    new DialoguePage("Petra la Geologue",
                            "J'ai besoin de deux cents minerais de cuivre pour reconstituer ma collection. C'est pour la science !")
                ),

                null,
                null,
                1440
        );
    }
}
