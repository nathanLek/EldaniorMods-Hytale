package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.classes.enums.ClassType;
import com.eldanior.system.classes.enums.Rarity;

public class Mage extends ClassModel {

    public Mage() {
        super(
                "mage",
                "Mage",
                "Un maitre des arcanes utilisant le mana.",
                Rarity.COMMON,
                ClassType.MAGE,
                null,
                20,
                false,
                0, 0, 1, 8, 2, 1
        );
    }
}