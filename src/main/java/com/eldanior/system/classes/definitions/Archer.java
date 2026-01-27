package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.classes.enums.ClassType;
import com.eldanior.system.classes.enums.Rarity;

public class Archer extends ClassModel {

    public Archer() {
        super(
                "archer",
                "Archer",
                "Un expert du tir a distance.",
                Rarity.COMMON,
                ClassType.ARCHER,
                null,
                20,
                false,
                2,
                2, 2, 0, 3, 7
        );
    }
}