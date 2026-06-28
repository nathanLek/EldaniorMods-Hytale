package com.eldanior.system.quest.definitions.secondaire.exploration;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Exploration N10 : L'Inventaire du Contrebandier.
 * Un ancien contrebandier repenti veut recuperer ses marchandises cachees.
 * Difficulte B - 18 coffres a decouvrir.
 */
public class SecQuest_Exploration_N10 extends NpcDialogueQuest {

    public SecQuest_Exploration_N10() {
        super(
                "sec_exploration_10",
                "L'Inventaire du Contrebandier",
                "Un ancien contrebandier repenti vous charge de retrouver ses marchandises dissimulees.",
                QuestType.EXPLORATION, QuestCategory.SECONDAIRE, QuestDifficulty.B,
                null, 18,
                3200, 10000, null,
                null,
                "Quest_Npc_Secondaire_Exploration_N10",

                List.of(
                    new DialoguePage("Felix le Repenti",
                            "Ne me juge pas trop vite, aventurier. " +
                            "J'ai ete contrebandier pendant quinze ans. " +
                            "Pas par choix, mais par necessite.\n\n" +
                            "Aujourd'hui, j'ai change de vie. Mais mes anciennes " +
                            "marchandises sont toujours la, cachees aux quatre coins " +
                            "de la region.",
                            "Un homme marque par les annees, une cicatrice au menton"),

                    new DialoguePage("Felix le Repenti",
                            "Des coffres remplis de toutes sortes de choses. " +
                            "Certaines legales, d'autres... disons que la provenance " +
                            "est discutable.\n\n" +
                            "Mais le temps a passe, et plus personne ne reclamera " +
                            "ces marchandises. Autant qu'elles servent a quelqu'un.",
                            "Il baisse la voix en regardant autour de lui"),

                    new DialoguePage("Felix le Repenti",
                            "Trouve 18 coffres. C'est une saree expedition, " +
                            "je te previens. Mes caches sont bien dissimulees.\n\n" +
                            "Sous des rochers, dans des arbres creux, derriere des cascades... " +
                            "J'etais plutot doue pour cacher mes affaires.\n\n" +
                            "Bonne chasse, l'ami.",
                            "Il vous fait un clin d'oeil complice")
                ),

                null,
                null,
                1440
        );
    }
}
