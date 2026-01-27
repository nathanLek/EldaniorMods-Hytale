package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.classes.enums.ClassType;
import com.eldanior.system.classes.enums.Rarity;

public class Warrior extends ClassModel {

    public Warrior() {
        super(
                "warrior",
                "Guerrier",
                "Un combattant robuste specialise dans le corps a corps.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                null,
                20,
                false,
                5,
                5, 5, 0, 3, 1
        );
    }
}