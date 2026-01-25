package com.eldanior.system.rpg.classes.definitions;

import com.eldanior.system.rpg.classes.ClassModel;
import com.eldanior.system.rpg.enums.ClassType;
import com.eldanior.system.rpg.enums.Rarity;

public class Merchant extends ClassModel {

    public Merchant() {
        super(
                "merchant",
                "Marchand",
                "Un negociateur hors pair.",
                Rarity.COMMON,
                ClassType.MERCHANT,
                null,
                true,
                "tycoon",
                0,
                false,
                1, 2, 2, 2, 1, 8
        );
    }
}