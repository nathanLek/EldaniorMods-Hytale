package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N5 : Le Chasseur de Coffres.
 * Un marchand itinerant cherche des coffres pour alimenter son commerce.
 * Difficulte D - 10 coffres a decouvrir.
 */
public class SecQuest_Exploration_N5 extends NpcDialogueQuest {

    public SecQuest_Exploration_N5() {
        super(
                "sec_exploration_5",
                "Le Chasseur de Coffres",
                "Un marchand itinerant vous propose un marche : trouvez des coffres pour alimenter son commerce.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.D,
                null, 10,
                1000, 3000, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N5",

                List.of(
                    new DialoguePage("Gaspard le Chineur",
                            "Psst ! Toi, l'aventurier. Tu cherches a te faire de l'or ? " +
                            "J'ai un deal pour toi.\n\n" +
                            "Je suis Gaspard, marchand specialise dans les objets anciens. " +
                            "Mon probleme ? Mon stock est vide. Et mes clients attendent.",
                            "Un homme a l'air malin avec un chariot rempli de bric-a-brac"),

                    new DialoguePage("Gaspard le Chineur",
                            "Les coffres abandonnes dans la nature contiennent toujours " +
                            "des merveilles. Armes rouillees, bijoux ternis, parchemins moisis... " +
                            "Pour la plupart des gens, c'est de la camelote.\n\n" +
                            "Pour moi, c'est de l'or en barre ! " +
                            "Tout est dans la facon de le presenter au client.",
                            "Il frotte une vieille piece avec un sourire gourmand"),

                    new DialoguePage("Gaspard le Chineur",
                            "Trouve-moi 10 coffres. Ouvre-les, prends ce qui t'interesse, " +
                            "et reviens me dire ou ils etaient.\n\n" +
                            "Je m'occupe du reste. Et toi, tu repars avec une belle bourse " +
                            "bien remplie. Marche conclu ?",
                            "Il vous tend la main pour sceller l'accord")
                ),

                null,
                null,
                1440
        );
    }
}
