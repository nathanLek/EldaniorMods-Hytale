package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SeigneurdesVenins extends ClassModel {
    public SeigneurdesVenins() {
        super("seigneur_des_venins", "Seigneur des Venins", "Le seigneur de tous les venins. Chaque toxine lui obeit.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.SPIRIT_DRAIN, PassiveSkill.HYDRA_BLOOD, PassiveSkill.FATAL_PRECISION), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                98, 98, 72, 72, 200, 242);
    }
}
