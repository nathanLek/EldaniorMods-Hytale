package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;

import java.util.List;

public class Novice extends ClassModel {

    public Novice() {
        super(
                "novice",
                "Novice",
                "Juste un Novice.",
                Rarity.COMMON,
                ClassType.NOVICE,
                List.of(),
                null,
                0,
                false,
                0, 0, 0, 0, 0, 0
        );
    }
}