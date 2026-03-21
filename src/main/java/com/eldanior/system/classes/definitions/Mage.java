package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;

import java.util.List;

public class Mage extends ClassModel {

    public Mage() {
        super(
                "mage",
                "Mage",
                "Un maitre des arcanes utilisant le mana.",
                Rarity.COMMON,
                ClassType.MAGE,
                List.of(),
                List.of(),
                120,
                false,
                3, 3, 12, 2, 2, 3
        );
    }
}