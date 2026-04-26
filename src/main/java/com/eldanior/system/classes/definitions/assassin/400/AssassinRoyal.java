package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class AssassinRoyal extends ClassModel {
    public AssassinRoyal() {
        super("assassin_royal", "Assassin Royal", "L'assassin des rois, forme pour eliminer les cibles les plus protegees.",
                Rarity.RARE, ClassType.ASSASSIN, List.of(PassiveSkill.DEADLY_PRECISION, PassiveSkill.PRESSURE_POINT, PassiveSkill.KEEN_SENSES), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                14, 6, 4, 2, 28, 24);
    }
}
