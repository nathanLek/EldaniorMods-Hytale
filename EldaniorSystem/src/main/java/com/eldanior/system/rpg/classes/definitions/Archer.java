package com.eldanior.system.rpg.classes.definitions;

import com.eldanior.system.rpg.classes.ClassModel;
import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;

public class Archer extends ClassModel {

    public Archer() {
        super(
                "archer",
                "Archer",
                "Un expert du tir a distance.",
                Rarity.COMMON,
                ClassType.ARCHER,
                null,
                true,
                "ranger",
                0,
                false,
                2, 2, 0, 3, 7, 1
        );
    }
}