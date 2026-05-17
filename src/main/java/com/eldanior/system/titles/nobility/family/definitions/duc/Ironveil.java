package com.eldanior.system.titles.nobility.family.definitions.duc;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Ironveil extends NobleFamilyModel {

    public Ironveil() {
        super(
                "ironveil",
                "Ironveil",
                "Notre volonte est d'acier, notre honneur indestructible.",
                "Les Ironveil sont une lignee de guerriers indomptables, vassaux fideles des Runkandel. "
                + "Leur fondateur, Ser Aldric Ironveil, forgea lui-meme l'armure legendaire du Voile de Fer "
                + "qui le rendit invincible sur le champ de bataille. Les soldats Ironveil sont reconnus "
                + "pour leur discipline de fer et leur loyaute absolue. Aucun Ironveil n'a jamais trahi son serment.",
                Rarity.EPIC,
                NobilityRank.DUC,
                PassiveSkill.FAMILY_IRON_WILL
        );
    }
}
