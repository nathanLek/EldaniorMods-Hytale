package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class SeigneurduVenin extends ClassModel {
    public SeigneurduVenin() {
        super("seigneur_du_venin", "Seigneur du Venin", "Le maitre absolu des poisons. Son souffle seul est mortel.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.HYDRA_BLOOD, PassiveSkill.SPIRIT_DRAIN, PassiveSkill.ABSOLUTE_PRECISION), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                68, 68, 52, 34, 138, 156);
    }
}
