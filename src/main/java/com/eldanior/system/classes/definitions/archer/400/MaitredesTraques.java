package com.eldanior.system.classes.definitions.archer;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class MaitredesTraques extends ClassModel {
    public MaitredesTraques() {
        super("maitre_des_traques", "Maitre des Traques", "Le maitre inconteste de la traque. Nul ne peut se cacher de lui.",
                Rarity.EPIC, ClassType.ARCHER, List.of(PassiveSkill.BLOOD_HUNT, PassiveSkill.DEADLY_PRECISION, PassiveSkill.HAWK_EYE), List.of(WeaponMastery.BOW, WeaponMastery.DAGGER), List.of(), 400, false,
                36, 32, 10, 24, 92, 76);
    }
}
