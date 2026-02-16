package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;

public class Novice extends ClassModel {

    public Novice() {
        super(
                "novice",
                "Novice",
                "Juste un Novice.",
                Rarity.COMMON,
                ClassType.NOVICE,
                null,
                0,
                false,
                0, 0, 0, 0, 0, 0
        );
    }
}