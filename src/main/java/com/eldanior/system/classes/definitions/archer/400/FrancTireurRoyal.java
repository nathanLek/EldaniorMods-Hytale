package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class FrancTireurRoyal extends ClassModel {
    public FrancTireurRoyal() {
        super("franc_tireur_royal", "Franc Tireur Royal", "Le tireur d'elite du roi, charge d'eliminer les menaces a distance.",
                Rarity.RARE, ClassType.ARCHER, List.of(PassiveSkill.HAWK_EYE, PassiveSkill.LIGHT_REFLEXES, PassiveSkill.DEADLY_PRECISION), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                12, 8, 6, 6, 26, 26);
    }
}
