package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class Archer extends ClassModel {

    public Archer() {
        super(
                "archer",
                "Archer",
                "Un expert du tir a distance.",
                Rarity.COMMON,
                ClassType.ARCHER,
                List.of(),
                List.of(WeaponMastery.BOW, WeaponMastery.DAGGER),
                List.of(),
                120,
                false,
                2, 2, 2, 0, 3, 7
        );
    }
}