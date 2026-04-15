package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class Assassin extends ClassModel {

    public Assassin() {
        super(
                "assassin",
                "Assassin",
                "Un tueur silencieux qui frappe dans l'ombre.",
                Rarity.COMMON,
                ClassType.ASSASSIN,
                List.of(),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of(),
                120,
                false,
                4, 3, 3, 2, 10, 3
        );
    }
}