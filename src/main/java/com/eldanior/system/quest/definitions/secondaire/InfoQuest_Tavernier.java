package com.eldanior.system.quest.definitions.secondaire;

import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Dialogue informatif du Tavernier.
 * Ne donne pas de quete - sert a valider l'objectif "Parler au Tavernier"
 * de la quete de l'Ancien Conseiller.
 */
public class InfoQuest_Tavernier extends NpcDialogueQuest {

    public InfoQuest_Tavernier() {
        super(
                "talk_tavernier",
                "Informations sur le Dragon",
                "Le tavernier connait des histoires sur le Dragon Primordial.",
                "Quest_Npc_Secondaire_Indice_N1",

                List.of(
                    new DialoguePage("Alex Silford",
                            "Ah, un aventurier ! Asseyez-vous, asseyez-vous...\n\n" +
                            "Vous cherchez des informations sur le Dragon Primordial ? " +
                            "Peu de gens osent prononcer son nom de nos jours.\n\n" +
                            "Laissez-moi vous raconter ce que mon grand-pere m'a dit...",
                            "L'interieur chaleureux d'une taverne"),

                    new DialoguePage("Alex Silford",
                            "Le Dragon Primordial vit au-dela des Terres du Vide, " +
                            "dans un sanctuaire que personne n'a vu depuis des siecles.\n\n" +
                            "On raconte que pour y acceder, il faut posseder un Coeur de Dragon — " +
                            "un artefact extremement rare qui pulse d'energie ancienne.\n\n" +
                            "Les anciens disaient que ces coeurs se trouvent au plus profond " +
                            "des donjons les plus dangereux du monde.",
                            "Il baisse la voix en regardant autour de lui"),

                    new DialoguePage("Alex Silford",
                            "Si vous etes vraiment serieux dans votre quete, " +
                            "voici mon conseil : trouvez un Coeur de Dragon, " +
                            "puis retournez voir l'Ancien Conseiller.\n\n" +
                            "Il saura quoi en faire. C'est lui qui connait le chemin.\n\n" +
                            "Bonne chance, aventurier. Vous en aurez besoin.",
                            "Il vous sert un dernier verre")
                ),

                "talk_tavernier" // Valide l'objectif "parler au tavernier"
        );
    }
}
