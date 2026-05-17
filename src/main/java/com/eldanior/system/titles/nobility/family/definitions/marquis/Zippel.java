package com.eldanior.system.titles.nobility.family.definitions.marquis;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Zippel extends NobleFamilyModel {

    public Zippel() {
        super(
                "zippel",
                "Zippel",
                "La magie est l'arme supreme, et nous en sommes les maitres.",
                "Les Zippel sont les plus grands arcanistes que le royaume ait jamais connus. "
                + "Leur ancetre, Thalion Zippel, decouvrit les Sources Arcanes cachees sous les montagnes du nord-ouest "
                + "et fonda la premiere Academie de Magie. Chaque generation produit des mages d'une puissance redoutable. "
                + "Leur marquisat est un bastion du savoir magique, ou les tours d'etude s'elevent au-dessus des nuages.",
                Rarity.LEGENDARY,
                NobilityRank.MARQUIS,
                PassiveSkill.FAMILY_PHOENIX_BLOOD
        );
    }
}