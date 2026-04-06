package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class AvantGarde extends ClassModel {

    public AvantGarde() {
        super(
                "avant_garde",
                "Avant-Garde",
                "Un combattant d'elite formant la premiere ligne de defense. Son armure est sa meilleure arme.",
                Rarity.RARE,
                ClassType.WARRIOR,
                List.of(),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of(),
                250,
                false,
                15, 40, 5, 35, 10, 5
        );
    }
}