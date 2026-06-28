package com.eldanior.system.quest.definitions.secondaire;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete secondaire : L'Epopee d'un Roi.
 * L'ancien conseiller du Roi Fou raconte l'histoire du Dragon Primordial.
 * Objectifs: Lv200, parler au Tavernier (PNJ info dragon), trouver un Coeur de Dragon.
 * Debloque la quete principale "La Naissance d'un Roi".
 */
public class SecQuest_AncienConseiller extends NpcDialogueQuest {

    public SecQuest_AncienConseiller() {
        super(
                "sec_ancien_conseiller",
                "L'Epopee d'un Roi",
                "Rencontrez l'ancien conseiller du Roi Fou et decouvrez les secrets du royaume.",
                QuestType.COLLECTION, QuestCategory.SECONDAIRE, QuestDifficulty.A,
                null, 1,
                5000, 50000, null,
                null,
                "Quest_Npc_Secondaire_Multiple_N1", // NPC ID

                // === PAGES DE DIALOGUE ===
                List.of(
                    new DialoguePage("Ancien Conseiller",
                            "Bienvenue, jeune aventurier... Vous etes venu chercher la verite ?\n\n" +
                            "Il y a bien longtemps, le Roi Fou regnait sur Eldanior. Son pouvoir etait immense, " +
                            "mais sa folie l'a consume. Le royaume a sombre dans le chaos...\n\n" +
                            "Seul un elu pourra restaurer l'ordre.",
                            "Un vieil homme dans une tour en ruine"),

                    new DialoguePage("Ancien Conseiller",
                            "Le Dragon Primordial... C'est lui qui choisit le Roi. " +
                            "Pas par la force, mais par la sagesse et la determination.\n\n" +
                            "Le dernier Roi a echoue car il a cherche le pouvoir pour lui-meme. " +
                            "Le prochain devra etre different...\n\n" +
                            "Etes-vous pret a affronter ce destin ?",
                            "Des fresques anciennes montrent un dragon dore"),

                    new DialoguePage("Ancien Conseiller",
                            "Voici ce que vous devez faire :\n\n" +
                            "D'abord, atteignez le niveau 200 — seul un guerrier aguerri peut affronter ce destin.\n\n" +
                            "Ensuite, trouvez le Tavernier du village. Il connait des choses sur le Dragon Primordial " +
                            "que personne d'autre n'oserait raconter. Parlez-lui.\n\n" +
                            "Enfin, rapportez-moi un Coeur de Dragon. C'est la preuve ultime de votre valeur.\n\n" +
                            "Revenez me voir quand tout sera accompli.",
                            "Il vous tend un parchemin ancien")
                ),

                // === OBJECTIFS DE COMPLETION ===
                new QuestCondition()
                        .level(200)
                        .questCompleted("talk_tavernier") // Avoir parle au Tavernier
                        .item("DragonHeart"),             // Coeur de Dragon

                // === DEBLOQUE ===
                "main_naissance_roi" // ID de la quete principale a debloquer
        );
    }
}
