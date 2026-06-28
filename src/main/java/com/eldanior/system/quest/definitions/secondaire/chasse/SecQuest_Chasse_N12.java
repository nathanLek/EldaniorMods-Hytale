package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N12 extends NpcDialogueQuest {
    public SecQuest_Chasse_N12() {
        super(
                "sec_chasse_n12",
                "Le Golem de Cristal",
                "Detruire le golem de cristal qui bloque l'acces a la mine.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.S,
                "golem crystal", 1,
                8000, 6000, "brise_golem",
                null, "Quest_Npc_Secondaire_Chasse_N12",
                List.of(
                        new DialoguePage("Orik le Barbare", "ORIK ! BARBARE ! MOI AVOIR PROBLEME GROS COMME MONTAGNE ! Toi ecouter bien, petit humain."),
                        new DialoguePage("Orik le Barbare", "GOLEM DE CRISTAL DANS MONTAGNE ! Moi taper avec massue, massue casser. Moi taper avec roche, roche exploser. Golem pas bouger du tout !"),
                        new DialoguePage("Orik le Barbare", "TOI PLUS MALIN QUE ORIK, toi trouver moyen detruire cristal. Si toi reussir, Orik donner tout tresor de caverne. Et titre aussi !")
                ),
                null, null, 1440
        );
    }
}
