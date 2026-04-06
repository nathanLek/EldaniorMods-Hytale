package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class Brute extends ClassModel {

    public Brute() {
        super(
                "brute",
                "Brute",
                "Un combattant instinctif et sauvage qui compte uniquement sur sa force brute.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                250,
                false,
                15, 10, 1, 4, 4, 2
        );
    }
}