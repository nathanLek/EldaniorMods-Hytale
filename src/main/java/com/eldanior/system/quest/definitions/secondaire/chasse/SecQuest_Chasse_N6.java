package com.eldanior.system.quest.definitions.secondaire.chasse;

import com.eldanior.system.quest.QuestCategory;
import com.eldanior.system.quest.QuestDifficulty;
import com.eldanior.system.quest.QuestType;
import com.eldanior.system.quest.dialogue.DialoguePage;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;

import java.util.List;

public class SecQuest_Chasse_N6 extends NpcDialogueQuest {
    public SecQuest_Chasse_N6() {
        super(
                "sec_chasse_n6",
                "Le Dragon des Marais",
                "Terrasser les dragons qui ont elu domicile dans les marais.",
                QuestType.CHASSE, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                "dragon", 3,
                5000, 4000, "tueur_de_dragons",
                null, "Quest_Npc_Secondaire_Chasse_N6",
                List.of(
                        new DialoguePage("Marcus le Chevalier", "Je suis Sir Marcus, chevalier de l'Ordre du Phenix. J'ai traque ces creatures pendant des mois, mais trois dragons se sont installes dans les marais de l'Est."),
                        new DialoguePage("Marcus le Chevalier", "Chacun d'eux crache un feu capable de faire fondre l'acier. Mes hommes sont tombes un par un. Je suis le dernier survivant de mon bataillon."),
                        new DialoguePage("Marcus le Chevalier", "Seul un veritable heros peut accomplir cette tache. Si tu y parviens, tu gagneras le titre de Tueur de Dragons.")
                ),
                null, null, 1440
        );
    }
}
