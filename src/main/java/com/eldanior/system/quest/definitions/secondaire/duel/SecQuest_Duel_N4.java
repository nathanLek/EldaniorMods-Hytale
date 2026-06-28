package com.eldanior.system.quest.definitions.secondaire.duel;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire Duel N4 : L'Honneur des Lames
 * Difficulte E - Gagner 5 duels
 */
public class SecQuest_Duel_N4 extends NpcDialogueQuest {

    public SecQuest_Duel_N4() {
        super(
                "sec_duel_4",
                "L'Honneur des Lames",
                "Un chevalier deshonore vous demande de restaurer la reputation de son ordre.",
                QuestType.DUEL, QuestCategory.SECONDAIRE, QuestDifficulty.E,
                null, 5,
                700, 2500, null,
                null,
                "Quest_Npc_Secondaire_Duel_N4",

                List.of(
                    new DialoguePage("Ser Aldric le Banni",
                            "Autrefois, je portais les couleurs de l'Ordre de l'Aube. " +
                            "Un ordre de chevaliers dont le nom seul faisait trembler les ennemis.\n\n" +
                            "Puis la trahison d'un de nos freres a tout detruit. " +
                            "Mon rang, mon honneur, mon foyer...",
                            "Un homme en armure ternie, le regard hante"),

                    new DialoguePage("Ser Aldric le Banni",
                            "Je ne peux plus me battre moi-meme. Mes blessures me l'interdisent. " +
                            "Mais toi, tu pourrais combattre en mon nom.\n\n" +
                            "Cinq duels gagnes au nom de l'Ordre de l'Aube. " +
                            "Montre au monde que notre art du combat n'est pas mort.",
                            "Il serre le pommeau de son epee brisee"),

                    new DialoguePage("Ser Aldric le Banni",
                            "Chaque victoire est une pierre posee sur le chemin de la redemption. " +
                            "Cinq pierres, et peut-etre que l'honneur des Lames revivra.\n\n" +
                            "Acceptes-tu ce fardeau, aventurier ?",
                            "Une larme coule sur sa joue burinee")
                ),
                null, null, 1440
        );
    }
}
