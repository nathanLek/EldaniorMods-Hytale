package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

/**
 * Mousquetaire (demande du 2026-11-15) : seule classe qui manie les armes a feu. Depuis le 2026-11-16 (demande de
 * l'utilisateur) ce n'est plus une classe de depart : c'est une evolution de l'Archer (Archer.java, palier 180), elle-meme
 * SANS evolution. Armes a feu : mousquets, mousquets legendaires et leurs transformations (MasterySystem) ; arc, rapiere et
 * dague en appoint.
 * Force = degats de la balle, Agilite = rechargement, Endurance = portee et lunette (Mousquet.java). Rune de classe :
 * Rune de Transformation d'Arme (mousquets legendaires).
 */
public class Mousquetaire extends ClassModel {

    public static final String ID = "mousquetaire";

    public Mousquetaire() {
        super(
                ID,
                "Mousquetaire",
                "Le Mousquetaire est le seul combattant a maitriser les armes a feu : mousquet a la lunette, baionnette au "
                        + "corps a corps, et les mousquets legendaires. Evolution de l'Archer, sans evolution.",
                Rarity.COMMON,
                ClassType.MOUSQUETAIRE,
                List.of(PassiveSkill.TIRELESS_BREATH.r(1)),
                List.of(WeaponMastery.GUN, WeaponMastery.RIFLE, WeaponMastery.BOW, WeaponMastery.SWORD, WeaponMastery.DAGGER),
                List.of(),
                0,
                false,
                12, 6, 2, 12, 14, 4                                              // palier des evolutions de l'Archer (total 50)
        );
        tier(2);                                                               // palier 2 (evolution de l'Archer)
        runes("Rune_TransformationArme");                                      // arme de la classe : toujours permise
    }
}
