package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.classes.enums.ClassType;
import com.eldanior.system.classes.enums.Rarity;

public class Merchant extends ClassModel {

    public Merchant() {
        super(
                "merchant",
                "Marchand",
                "Un negociateur hors pair.",
                Rarity.COMMON,
                ClassType.MERCHANT,
                null,
                20,
                false,
                2, 1, 2, 2, 2, 10
        );
    }
}