package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AscendantdelArc extends ClassModel {
    public AscendantdelArc() {
        super("ascendant_de_l_arc", "Ascendant de l'Arc", "L'archer ascendant qui s'eleve au-dela des limites mortelles.",
                Rarity.LEGENDARY, ClassType.ARCHER, List.of(PassiveSkill.ABYSS_BLADE, PassiveSkill.CREATOR_PRECISION, PassiveSkill.GOD_SLAYER_SWIFTNESS), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                170, 136, 68, 102, 340, 340);
    }
}
