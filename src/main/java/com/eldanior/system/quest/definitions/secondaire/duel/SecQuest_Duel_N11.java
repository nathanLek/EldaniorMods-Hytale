package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N11 : La Rancon de la Gloire
 * Difficulte C - Gagner 15 duels
 */
public class SecQuest_Duel_N11 extends NpcDialogueQuest {

    public SecQuest_Duel_N11() {
        super(
                "sec_duel_11",
                "La Rancon de la Gloire",
                "Un ancien champion decheant cherche un successeur pour racheter ses erreurs.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.C,
                null, 15,
                2000, 7000, null,
                null,
                "Quest_Npc_Secondaire_Duel_N11",

                List.of(
                    new DialoguePage("Dorian Lame-Brisee",
                            "On m'acclamait autrefois. Le Champion Invaincu, ils m'appelaient. " +
                            "Cent duels, cent victoires.\n\n" +
                            "Et puis j'ai commence a tricher. Des poisons sur ma lame, " +
                            "des coups bas quand l'arbitre ne regardait pas. " +
                            "La victoire est devenue une drogue.",
                            "Un homme amaigri, les yeux cernes, assis dans une taverne sombre"),

                    new DialoguePage("Dorian Lame-Brisee",
                            "Quand ils ont decouvert la verite, j'ai tout perdu. " +
                            "Mon titre, mes richesses, mes amis. Tout.\n\n" +
                            "Aujourd'hui, je cherche quelqu'un qui peut gagner proprement. " +
                            "Quinze duels honnetes. C'est ma penitence autant que la tienne.",
                            "Il fixe son verre vide"),

                    new DialoguePage("Dorian Lame-Brisee",
                            "Gagne ces quinze duels avec honneur, et je saurai " +
                            "que tout n'est pas perdu. Que le combat peut encore etre noble.\n\n" +
                            "Fais-le pour toi. Moi, c'est trop tard.",
                            "Il detourne le regard vers la fenetre")
                ),
                null, null, 1440
        );
    }
}
