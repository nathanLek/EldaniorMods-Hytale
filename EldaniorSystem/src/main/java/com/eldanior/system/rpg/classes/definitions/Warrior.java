package com.eldanior.system.rpg.classes.definitions;

import com.eldanior.system.rpg.classes.ClassModel;
import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;

public class Warrior extends ClassModel {

    public Warrior() {
        super(
                "warrior",
                "Guerrier",
                "Un combattant robuste specialise dans le corps a corps.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                null,
                true,
                "paladin",
                0,
                false,
                5, 5, 0, 3, 1, 0
        );
    }
}