package com.eldanior.system.quest.definitions.principal;

import com.eldanior.system.quest.*;
import com.eldanior.system.quest.dialogue.*;

import java.util.List;

/**
 * Quete principale : La Naissance d'un Roi.
 * Un villageois (tavernier) donne des indices pour trouver le Dragon Primordial.
 * Debloquee apres la quete secondaire "L'Epopee d'un Roi".
 * Condition: Lv250 + objet + quete precedente.
 * Recompense: devenir ROI + XP + Or + Titre.
 */
public class MainQuest_NaissanceRoi extends NpcDialogueQuest {

    public MainQuest_NaissanceRoi() {
        super(
                "main_naissance_roi",
                "La Naissance d'un Roi",
                "Un villageois connait le chemin vers le Dragon Primordial. Trouvez-le et prouvez que vous etes digne.",
                QuestType.CHASSE, QuestCategory.PRINCIPAL, QuestDifficulty.S,
                null, 1,
                50000, 500000, "first_king",
                null,
                "AncienConseiller_Npc", // Donné par l'Ancien Conseiller après la quête précédente

                // === PAGES DE DIALOGUE ===
                List.of(
                    new DialoguePage("Tavernier",
                            "Ah, vous voila... L'ancien conseiller m'a prevenu de votre venue.\n\n" +
                            "Vous cherchez le Dragon Primordial ? Beaucoup ont essaye avant vous. " +
                            "Aucun n'est revenu. Mais vous... vous avez quelque chose de different dans le regard.\n\n" +
                            "Asseyez-vous, je vais vous raconter ce que je sais.",
                            "L'interieur d'une taverne chaleureuse"),

                    new DialoguePage("Tavernier",
                            "Le Dragon Primordial vit dans un sanctuaire cache au-dela des Terres du Vide. " +
                            "Pour y acceder, il vous faut la Couronne Primordiale — un artefact forge " +
                            "dans les flammes du premier age.\n\n" +
                            "On dit qu'elle se trouve dans le coffre le plus profond des ruines anciennes. " +
                            "Mais attention, les gardiens sont feroces.\n\n" +
                            "Une fois la couronne en main, dirigez-vous vers le nord, " +
                            "au-dela de la foret des ames perdues.",
                            "Une carte ancienne etalee sur la table"),

                    new DialoguePage("Tavernier",
                            "Quand vous serez face au Dragon, ne montrez aucune peur. " +
                            "Il peut lire dans les ames. Si vous etes digne, il vous couronnera.\n\n" +
                            "Si vous ne l'etes pas... eh bien, personne ne peut vous sauver la-bas.\n\n" +
                            "Allez, heros. Le destin du royaume repose sur vos epaules. " +
                            "Et si vous reussissez... revenez boire un verre, la premiere tournee sera pour moi !",
                            "Le tavernier vous serre la main avec un sourire")
                ),

                // === CONDITION DE COMPLETION ===
                new QuestCondition()
                        .level(250)
                        .item("Hytale:royal_crown")
                        .questCompleted("sec_ancien_conseiller"),

                // === DEBLOQUE ===
                null // Fin de la chaine
        );
    }
}
