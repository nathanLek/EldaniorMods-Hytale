package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class Templier extends ClassModel {

    public Templier() {
        super(
                "templier",
                "Templier",
                "Un Templier robuste specialise dans le corps a corps.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                250,
                false,
                25, 25, 2, 25, 30, 30
        );
    }
}