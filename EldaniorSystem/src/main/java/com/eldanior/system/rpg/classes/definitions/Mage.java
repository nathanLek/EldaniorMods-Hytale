package com.eldanior.system.rpg.classes.definitions;

import com.eldanior.system.rpg.classes.ClassModel;
import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;

public class Mage extends ClassModel {

    public Mage() {
        super(
                "mage",
                "Mage",
                "Un maitre des arcanes utilisant le mana.",
                Rarity.COMMON,
                ClassType.MAGE,
                null,
                true,
                "archmage",
                0,
                false,
                0, 1, 8, 2, 1, 2
        );
    }
}