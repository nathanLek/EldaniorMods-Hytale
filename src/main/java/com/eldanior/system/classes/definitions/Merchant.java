package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;

import java.util.List;

public class Merchant extends ClassModel {

    public Merchant() {
        super(
                "merchant",
                "Marchand",
                "Un negociateur hors pair.",
                Rarity.COMMON,
                ClassType.MERCHANT,
                List.of(),
                null,
                20,
                false,
                2, 1, 2, 2, 2, 10
        );
    }
}