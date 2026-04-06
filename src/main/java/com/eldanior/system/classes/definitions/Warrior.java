package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;

import java.util.List;

public class Warrior extends ClassModel {

    public Warrior() {
        super(
                "warrior",
                "Guerrier",
                "Un combattant robuste specialise dans le corps a corps.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(),
                List.of(WeaponMastery.SWORD, WeaponMastery.AXE, WeaponMastery.SHIELD),
                List.of("templier", "epeiste", "fantassin", "brute", "mercenaire", "avant_garde", "ravageur", "bretteur", "lame_mage", "champion", "colosse", "executeur", "gardien_runique", "titan", "heros", "fleau", "sang_dragon", "DivineApotre"),
                120,
                false,
                5, 5, 2, 2, 3, 1
        );
    }
}