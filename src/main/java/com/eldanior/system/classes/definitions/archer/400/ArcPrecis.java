package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArcPrecis extends ClassModel {
    public ArcPrecis() {
        super("arc_precis", "Arc Precis", "La precision incarnee. Pas une fleche n'est gaspillee.",
                Rarity.RARE, ClassType.ARCHER, List.of(PassiveSkill.KEEN_SENSES, PassiveSkill.THUNDER_REFLEXES, PassiveSkill.PRESSURE_POINT), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                8, 12, 8, 8, 24, 22);
    }
}
