package com.eldanior.system.titles.nobility.family.definitions.marquis;

import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;

public class Runkandel extends NobleFamilyModel {

    public Runkandel() {
        super(
                "runkandel",
                "Runkandel",
                "Par l'epee et le bouclier, nous forgeons l'histoire du royaume.",
                "Les Runkandel sont la plus grande lignee de chevaliers et guerriers du royaume. "
                + "Depuis des generations, ils forment l'elite militaire de la couronne. Leur fondateur, "
                + "le general Vorn Runkandel, repoussa a lui seul une armee d'envahisseurs lors du Siege des Flammes. "
                + "Leur marquisat au nord-est est une terre de forteresses et de champs d'entrainement "
                + "ou chaque enfant apprend le maniement de l'epee avant de savoir lire.",
                Rarity.LEGENDARY,
                NobilityRank.MARQUIS,
                PassiveSkill.FAMILY_DRAGON_FURY
        );
    }
}