package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class TyrandesOmbres extends ClassModel {
    public TyrandesOmbres() {
        super("tyran_des_ombres", "Tyran des Ombres", "Le tyran absolu du royaume des ombres. Sa terreur est sans limites.",
                Rarity.LEGENDARY, ClassType.ASSASSIN, List.of(PassiveSkill.SOUL_CRUSHING_PRESSURE, PassiveSkill.ABSOLUTE_PRECISION, PassiveSkill.GOD_SLAYER_SWIFTNESS), List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD), List.of(), 400, false,
                170, 136, 52, 102, 374, 340);
    }
}
