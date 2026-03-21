package com.eldanior.system.classes.definitions.warrior;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;

import java.util.List;

public class Fantassin extends ClassModel {

    public Fantassin() {
        super(
                "fantassin",
                "Fantassin",
                "Un soldat discipline et entraine pour encaisser les coups sur la ligne de front.",
                Rarity.COMMON,
                ClassType.WARRIOR,
                List.of(),
                List.of(),
                250,
                false,
                8, 15, 2, 10, 3, 2
        );
    }
}