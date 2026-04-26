package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class ArcDivin extends ClassModel {
    public ArcDivin() {
        super("arc_divin_leg", "Arc Divin", "L'arc divin dont les fleches sont des jugements celestes.",
                Rarity.LEGENDARY, ClassType.ARCHER, List.of(PassiveSkill.GENESIS_EDGE, PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.COSMIC_CONSTITUTION), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                166, 140, 72, 98, 336, 336);
    }
}
