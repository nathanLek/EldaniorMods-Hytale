package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class PoisonPrimordial extends ClassModel {
    public PoisonPrimordial() {
        super("poison_primordial", "Poison Primordial", "Le tout premier poison, celui qui existait avant la creation.",
                Rarity.UNIQUE, ClassType.ASSASSIN, List.of(PassiveSkill.HYDRA_BLOOD, PassiveSkill.DEATH_HUNT, PassiveSkill.ABSOLUTE_PRECISION), List.of(WeaponMastery.DAGGER), List.of(), 400, false,
                102, 102, 68, 68, 204, 238);
    }
}
