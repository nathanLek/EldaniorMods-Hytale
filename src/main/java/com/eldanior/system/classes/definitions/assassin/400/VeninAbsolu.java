package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class VeninAbsolu extends ClassModel {
    public VeninAbsolu() {
        super("venin_absolu", "Venin Absolu", "Le poison ultime coule dans ses veines. Il est le venin incarne.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.SPIRIT_DRAIN, PassiveSkill.HYDRA_BLOOD, PassiveSkill.DEADLY_PRECISION), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                64, 72, 54, 36, 140, 148);
    }
}
